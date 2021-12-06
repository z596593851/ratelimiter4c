package com.github.ratelimiter4c.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class RollingNumber {

    private static final Time ACTUAL_TIME = new ActualTime();
    private final Time time;
    //统计周期
    final int timeInMilliseconds;
    //桶的数量
    final int numberOfBuckets;
    //每个桶的大小=统计周期/桶的数量
    final int bucketSizeInMillseconds;
    final BucketCircularArray buckets;
    private final CumulativeSum cumulativeSum = new CumulativeSum();

    public RollingNumber(int timeInMilliseconds, int numberOfBuckets) {
        this(ACTUAL_TIME, timeInMilliseconds, numberOfBuckets);
    }

    public RollingNumber(Time time, int timeInMilliseconds, int numberOfBuckets) {
        this.time = time;
        this.timeInMilliseconds = timeInMilliseconds;
        this.numberOfBuckets = numberOfBuckets;

        if (timeInMilliseconds % numberOfBuckets != 0) {
            throw new IllegalArgumentException("The timeInMilliseconds must divide equally into numberOfBuckets. For example 1000/10 is ok, 1000/11 is not.");
        }
        this.bucketSizeInMillseconds = timeInMilliseconds / numberOfBuckets;
        buckets = new BucketCircularArray(numberOfBuckets);
    }


    public void increment(EventType type) {
        getCurrentBucket().getAdder(type).increment();
    }

    public void add(EventType type, long value) {
        getCurrentBucket().getAdder(type).add(value);
    }

    public void reset() {
        // if we are resetting, that means the lastBucket won't have a chance to be captured in CumulativeSum, so let's do it here
        Bucket lastBucket = buckets.peekLast();
        if (lastBucket != null) {
            cumulativeSum.addBucket(lastBucket);
        }

        // clear buckets so we start over again
        buckets.clear();
    }

    public long getCumulativeSum(EventType type) {
        // this isn't 100% atomic since multiple threads can be affecting latestBucket & cumulativeSum independently
        // but that's okay since the count is always a moving target and we're accepting a "point in time" best attempt
        // we are however putting 'getValueOfLatestBucket' first since it can have side-affects on cumulativeSum whereas the inverse is not true
        return getValueOfLatestBucket(type) + cumulativeSum.get(type);
    }

    public long getRollingSum(EventType type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null) {
            return 0;
        }

        long sum = 0;
        for (Bucket b : buckets) {
            sum += b.getAdder(type).sum();
        }
        return sum;
    }

    public long getValueOfLatestBucket(EventType type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null) {
            return 0;
        }
        // we have bucket data so we'll return the lastBucket
        return lastBucket.get(type);
    }

    public long[] getValues(EventType type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null) {
            return new long[0];
        }
        // get buckets as an array (which is a copy of the current state at this point in time)
        Bucket[] bucketArray = buckets.getArray();
        // we have bucket data so we'll return an array of values for all buckets
        long[] values = new long[bucketArray.length];
        int i = 0;
        for (Bucket bucket : bucketArray) {
            values[i++] = bucket.getAdder(type).sum();
        }
        return values;
    }

    public long getRollingMaxValue(EventType type) {
        long[] values = getValues(type);
        if (values.length == 0) {
            return 0;
        } else {
            Arrays.sort(values);
            return values[values.length - 1];
        }
    }

    private final ReentrantLock newBucketLock = new ReentrantLock();

    private Bucket getCurrentBucket() {
        long currentTime = time.getCurrentTimeInMillis();
        Bucket currentBucket = buckets.peekLast();
        //在最后一个bucket的时间范围内就返回
        if (currentBucket != null && currentTime < currentBucket.windowStart + this.bucketSizeInMillseconds) {
            return currentBucket;
        }
        //否则创建
        if (newBucketLock.tryLock()) {
            try {
                //刚创建,size==0,则创建
                if (buckets.peekLast() == null) {
                    Bucket newBucket = new Bucket(currentTime);
                    buckets.addLast(newBucket);
                    return newBucket;
                } else {
                    //创建足够多的bucket直到赶上当前时间
                    for (int i = 0; i < numberOfBuckets; i++) {
                        Bucket lastBucket = buckets.peekLast();
                        if (currentTime < lastBucket.windowStart + this.bucketSizeInMillseconds) {
                            //只要在bucket的时间范围内就返回
                            //如果因为调度问题使我们处在了bucket之前这种奇怪的情形也不必担心
                            //只要不在bucket之后，就返回latest
                            return lastBucket;
                        } else if (currentTime - (lastBucket.windowStart + this.bucketSizeInMillseconds) > timeInMilliseconds) {
                            //如果经历的时间大于了整个计数周期，那么就清空所有并从头开始
                            reset();
                            //递归调用getCurrentBucket直到创建了一个新的bucket并返回它
                            return getCurrentBucket();
                        } else {
                            //紧挨着last的结束时间，创建一个新bucket放在最后
                            buckets.addLast(new Bucket(lastBucket.windowStart + this.bucketSizeInMillseconds));
                            //每当创建新bucket时,都会将老lastBucket累加进总数
                            cumulativeSum.addBucket(lastBucket);
                        }
                    }
                    return buckets.peekLast();
                }
            } finally {
                newBucketLock.unlock();
            }
        } else {
            //如果没有获取到锁，直接返回最新的一个bucket
            //这样获取到的bucket可能没有包含当前时间，这是避免锁竞争的折中选择，在统计时也是可容忍的
            currentBucket = buckets.peekLast();
            if (currentBucket != null) {
                return currentBucket;
            } else {
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                    // ignore
                }
                return getCurrentBucket();
            }
        }
    }

    interface Time {
        long getCurrentTimeInMillis();
    }

    private static class ActualTime implements Time {

        @Override
        public long getCurrentTimeInMillis() {
            return System.currentTimeMillis();
        }

    }

    static class Bucket {
        //所属时间段的开始时间
        final long windowStart;
        //每个元素代表了一种事件类型的计数值
        final LongAdder[] adderForCounterType;

        public Bucket(long startTime) {
            this.windowStart = startTime;
            adderForCounterType = new LongAdder[EventType.values().length];
            for(EventType type:EventType.values()){
                adderForCounterType[type.ordinal()] = new LongAdder();
            }
        }

        long get(EventType type) {
            return adderForCounterType[type.ordinal()].sum();
        }

        LongAdder getAdder(EventType type) {
            return adderForCounterType[type.ordinal()];
        }
    }

    /**
     * Cumulative counters (from start of JVM) from each Type
     */
    static class CumulativeSum {
        final LongAdder[] adderForCounterType;

        CumulativeSum() {
            adderForCounterType = new LongAdder[EventType.values().length];
            for(EventType type:EventType.values()){
                adderForCounterType[type.ordinal()] = new LongAdder();
            }
        }

        public void addBucket(Bucket lastBucket) {
            for (EventType type : EventType.values()) {
                getAdder(type).add(lastBucket.getAdder(type).sum());
            }
        }

        long get(EventType type) {
            return adderForCounterType[type.ordinal()].sum();
        }

        LongAdder getAdder(EventType type) {
            return adderForCounterType[type.ordinal()];
        }
    }


    static class BucketCircularArray implements Iterable<Bucket> {
        private final AtomicReference<ListState> state;
        // we don't resize, we always stay the same, so remember this
        private final int dataLength;
        private final int numBuckets;

        private class ListState {
            private final AtomicReferenceArray<Bucket> data;
            private final int size;
            private final int tail;
            private final int head;

            private ListState(AtomicReferenceArray<Bucket> data, int head, int tail) {
                this.head = head;
                this.tail = tail;
                if (head == 0 && tail == 0) {
                    size = 0;
                } else {
                    this.size = (tail + dataLength - head) % dataLength;
                }
                this.data = data;
            }

            public Bucket tail() {
                if (size == 0) {
                    return null;
                } else {
                    // we want to get the last item, so size()-1
                    return data.get(convert(size - 1));
                }
            }

            private Bucket[] getArray() {
                /*
                 * this isn't technically thread-safe since it requires multiple reads on something that can change
                 * but since we never clear the data directly, only increment/decrement head/tail we would never get a NULL
                 * just potentially return stale data which we are okay with doing
                 */
                ArrayList<Bucket> array = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    array.add(data.get(convert(i)));
                }
                return array.toArray(new Bucket[array.size()]);
            }

            private ListState incrementTail() {
                /* if incrementing results in growing larger than 'length' which is the max we should be at, then also increment head (equivalent of removeFirst but done atomically) */
                if (size == numBuckets) {
                    // increment tail and head
                    return new ListState(data, (head + 1) % dataLength, (tail + 1) % dataLength);
                } else {
                    // increment only tail
                    return new ListState(data, head, (tail + 1) % dataLength);
                }
            }

            public ListState clear() {
                return new ListState(new AtomicReferenceArray<Bucket>(dataLength), 0, 0);
            }

            public ListState addBucket(Bucket b) {
                //We could in theory have 2 threads addBucket concurrently and this compound operation would interleave.
                //This should NOT happen since getCurrentBucket is supposed to be executed by a single thread.
                //If it does happen, it's not a huge deal as incrementTail() will be protected by compareAndSet and one of the two addBucket calls will succeed with one of the Buckets.
                //In either case, a single Bucket will be returned as "last" and data loss should not occur and everything keeps in sync for head/tail.
                //Also, it's fine to set it before incrementTail because nothing else should be referencing that index position until incrementTail occurs.
                data.set(tail, b);
                return incrementTail();
            }

            // The convert() method takes a logical index (as if head was
            // always 0) and calculates the index within elementData
            private int convert(int index) {
                return (index + head) % dataLength;
            }
        }

        public BucketCircularArray(int size) {
            // + 1 as extra room for the add/remove;
            AtomicReferenceArray<Bucket> _buckets = new AtomicReferenceArray<>(size + 1);
            state = new AtomicReference<ListState>(new ListState(_buckets, 0, 0));
            dataLength = _buckets.length();
            numBuckets = size;
        }

        public void clear() {
            while (true) {
                /*
                 * it should be very hard to not succeed the first pass thru since this is typically is only called from
                 * a single thread protected by a tryLock, but there is at least 1 other place (at time of writing this comment)
                 * where reset can be called from (CircuitBreaker.markSuccess after circuit was tripped) so it can
                 * in an edge-case conflict.
                 * 
                 * Instead of trying to determine if someone already successfully called clear() and we should skip
                 * we will have both calls reset the circuit, even if that means losing data added in between the two
                 * depending on thread scheduling.
                 * 
                 * The rare scenario in which that would occur, we'll accept the possible data loss while clearing it
                 * since the code has stated its desire to clear() anyways.
                 */
                ListState current = state.get();
                //直接创建一个新的快照，并且cas替换
                ListState newState = current.clear();
                if (state.compareAndSet(current, newState)) {
                    return;
                }
            }
        }

        /**
         * Returns an iterator on a copy of the internal array so that the iterator won't fail by buckets being added/removed concurrently.
         */
        @Override
        public Iterator<Bucket> iterator() {
            return Collections.unmodifiableList(Arrays.asList(getArray())).iterator();
        }

        public void addLast(Bucket o) {
            ListState currentState = state.get();
            // create new version of state (what we want it to become)
            ListState newState = currentState.addBucket(o);

            /*
             * use compareAndSet to set in case multiple threads are attempting (which shouldn't be the case because since addLast will ONLY be called by a single thread at a time due to protection
             * provided in <code>getCurrentBucket</code>)
             */
            if (state.compareAndSet(currentState, newState)) {
                // we succeeded
                return;
            } else {
                // we failed, someone else was adding or removing
                // instead of trying again and risking multiple addLast concurrently (which shouldn't be the case)
                // we'll just return and let the other thread 'win' and if the timing is off the next call to getCurrentBucket will fix things
                return;
            }
        }

        public Bucket getLast() {
            return peekLast();
        }

        public int size() {
            // the size can also be worked out each time as:
            // return (tail + data.length() - head) % data.length();
            return state.get().size;
        }

        public Bucket peekLast() {
            return state.get().tail();
        }

        private Bucket[] getArray() {
            return state.get().getArray();
        }

    }

}

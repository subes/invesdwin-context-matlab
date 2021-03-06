package de.invesdwin.context.matlab.runtime.matconsolectl.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import org.springframework.beans.factory.FactoryBean;

import de.invesdwin.context.matlab.runtime.matconsolectl.pool.internal.MatlabProxyPoolableObjectFactory;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.pool.AObjectPool;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.duration.Duration;
import matlabcontrol.MatlabProxy;

@ThreadSafe
@Named
public final class MatlabProxyObjectPool extends AObjectPool<MatlabProxy>
        implements FactoryBean<MatlabProxyObjectPool> {

    public static final MatlabProxyObjectPool INSTANCE = new MatlabProxyObjectPool();

    private final WrappedExecutorService timeoutMonitorExecutor = Executors
            .newFixedCallerRunsThreadPool(getClass().getSimpleName() + "_timeout", 1);
    @GuardedBy("this")
    private final List<MatlabProxyWrapper> matlabProxyRotation = new ArrayList<MatlabProxyWrapper>();

    private MatlabProxyObjectPool() {
        super(MatlabProxyPoolableObjectFactory.INSTANCE);
        timeoutMonitorExecutor.execute(new MatlabProxyTimoutMonitor());
    }

    @Override
    protected synchronized MatlabProxy internalBorrowObject() {
        if (matlabProxyRotation.isEmpty()) {
            return factory.makeObject();
        }
        final MatlabProxyWrapper matlabProxy = matlabProxyRotation.remove(0);
        if (matlabProxy != null) {
            return matlabProxy.getMatlabProxy();
        } else {
            return factory.makeObject();
        }
    }

    @Override
    public synchronized int getNumIdle() {
        return matlabProxyRotation.size();
    }

    @Override
    public synchronized Collection<MatlabProxy> internalClear() {
        final Collection<MatlabProxy> removed = new ArrayList<MatlabProxy>();
        while (!matlabProxyRotation.isEmpty()) {
            removed.add(matlabProxyRotation.remove(0).getMatlabProxy());
        }
        return removed;
    }

    @Override
    protected synchronized MatlabProxy internalAddObject() {
        final MatlabProxy pooled = factory.makeObject();
        matlabProxyRotation.add(new MatlabProxyWrapper(factory.makeObject()));
        return pooled;
    }

    @Override
    protected synchronized void internalReturnObject(final MatlabProxy obj) {
        matlabProxyRotation.add(new MatlabProxyWrapper(obj));
    }

    @Override
    protected void internalInvalidateObject(final MatlabProxy obj) {
        //Nothing happens
    }

    @Override
    protected synchronized void internalRemoveObject(final MatlabProxy obj) {
        matlabProxyRotation.remove(new MatlabProxyWrapper(obj));
    }

    private class MatlabProxyTimoutMonitor implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Threads.throwIfInterrupted();
                    TimeUnit.MILLISECONDS.sleep(100);
                    synchronized (MatlabProxyObjectPool.this) {
                        if (!matlabProxyRotation.isEmpty()) {
                            final List<MatlabProxyWrapper> copy = new ArrayList<MatlabProxyWrapper>(
                                    matlabProxyRotation);
                            for (final MatlabProxyWrapper matlabProxy : copy) {
                                if (matlabProxy.isTimeoutExceeded()) {
                                    Assertions.assertThat(matlabProxyRotation.remove(matlabProxy)).isTrue();
                                }
                            }
                        }
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final class MatlabProxyWrapper {

        private final MatlabProxy matlabProxy;
        private final FDate timeoutStart;

        MatlabProxyWrapper(final MatlabProxy matlabProxy) {
            this.matlabProxy = matlabProxy;
            this.timeoutStart = new FDate();
        }

        public MatlabProxy getMatlabProxy() {
            return matlabProxy;
        }

        public boolean isTimeoutExceeded() {
            return new Duration(timeoutStart, new FDate()).isGreaterThan(Duration.ONE_MINUTE);
        }

        @Override
        public int hashCode() {
            return matlabProxy.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof MatlabProxyWrapper) {
                final MatlabProxyWrapper cObj = (MatlabProxyWrapper) obj;
                return matlabProxy.equals(cObj.getMatlabProxy());
            } else if (obj instanceof MatlabProxy) {
                return matlabProxy.equals(obj);
            } else {
                return false;
            }
        }

    }

    @Override
    public MatlabProxyObjectPool getObject() {
        return INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return MatlabProxyObjectPool.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}

package de.invesdwin.context.matlab.runtime.javasci;

import javax.annotation.concurrent.Immutable;
import javax.inject.Named;

import org.springframework.beans.factory.FactoryBean;

import de.invesdwin.context.matlab.runtime.contract.AScriptTaskMatlab;
import de.invesdwin.context.matlab.runtime.contract.IScriptTaskRunnerMatlab;
import de.invesdwin.context.matlab.runtime.javasci.internal.ScilabWrapper;
import de.invesdwin.util.error.Throwables;

@Immutable
@Named
public final class JavasciScriptTaskRunnerMatlab
        implements IScriptTaskRunnerMatlab, FactoryBean<JavasciScriptTaskRunnerMatlab> {

    public static final String CLEANUP_SCRIPT = "clear; clc";
    public static final String INTERNAL_RESULT_VARIABLE = "JSTRM_result";

    public static final JavasciScriptTaskRunnerMatlab INSTANCE = new JavasciScriptTaskRunnerMatlab();

    /**
     * public for ServiceLoader support
     */
    public JavasciScriptTaskRunnerMatlab() {
    }

    @Override
    public <T> T run(final AScriptTaskMatlab<T> scriptTask) {
        //get session
        final JavasciScriptTaskEngineMatlab engine = new JavasciScriptTaskEngineMatlab(ScilabWrapper.INSTANCE);
        engine.getSharedLock().lock();
        try {
            //inputs
            scriptTask.populateInputs(engine.getInputs());

            //execute
            scriptTask.executeScript(engine);

            //results
            final T result = scriptTask.extractResults(engine.getResults());
            engine.close();

            //return
            engine.getSharedLock().unlock();
            return result;
        } catch (final Throwable t) {
            engine.getSharedLock().unlock();
            throw Throwables.propagate(t);
        }
    }

    @Override
    public JavasciScriptTaskRunnerMatlab getObject() throws Exception {
        return INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return JavasciScriptTaskRunnerMatlab.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}

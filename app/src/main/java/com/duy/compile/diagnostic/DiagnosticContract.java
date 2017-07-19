package com.duy.compile.diagnostic;

import java.util.List;

import javax.tools.Diagnostic;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticContract {
    public interface View {
        public void display(List<Diagnostic> diagnostics);

        public void clear();

        public void setPresenter(Presenter presenter);
    }

    public interface Presenter {
        public void click(Diagnostic diagnostic);
    }
}

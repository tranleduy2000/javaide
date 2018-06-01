package com.duy.android.compiler.repo.maven;

public class MojoExecutionException extends Exception {
    public MojoExecutionException(String message, Exception ex) {
        super(message, ex);
    }
}

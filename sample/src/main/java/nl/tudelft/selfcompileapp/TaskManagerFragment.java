package nl.tudelft.selfcompileapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Keeps track of running tasks and progress. Updates activity gui if attached.
 * Starts optional intent after task is done.
 *
 * @author Paul Brussee
 */
public class TaskManagerFragment extends Fragment implements Handler.Callback {

    static final int TASK_CANCELED = 0;
    static final int TASK_FINISHED = -1;
    static final int TASK_PROGRESS = 1;
    protected Handler listener = new Handler(this);
    private int intProgress;
    private String strStatus;
    private Thread runningTask;
    private Intent done;

    int getProgress() {
        return intProgress;
    }

    String getStatus() {
        return strStatus;
    }

    boolean isIdle() {
        return runningTask == null;
    }

    //////////////////// FRAGMENT LIFECYCLE ////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    //////////////////// HANDLER CALLBACKS ////////////////////

    public boolean handleMessage(Message msg) {
        // update state
        switch (msg.what) {

            case TASK_FINISHED:
            case TASK_CANCELED:
                runningTask = null;
                intProgress = 0;
                strStatus = "";
                if (done != null) {
                    if (isAdded()) {
                        ((SelfCompileActivity) getActivity()).startActivity(done);
                    }
                    done = null;
                }
                break;

            case TASK_PROGRESS:
                intProgress = msg.arg1;
                if (msg.arg2 != 0) {
                    strStatus = getString(msg.arg2);
                }
                break;
        }

        // update gui
        if (isAdded()) {
            ((SelfCompileActivity) getActivity()).updateGui(isIdle());
        }
        return true;
    }

    //////////////////// TASKS ////////////////////

    void cancelTask(SelfCompileActivity activity, Intent done) {
        if (!isIdle()) {
            strStatus = activity.getString(R.string.stsCancel);
            activity.updateGui(false);
            this.done = done;
            runningTask.interrupt();
        }
    }

    void startClean(SelfCompileActivity activity, Intent done) {
        if (isIdle()) {
            activity.updateGui(false);
            this.done = done;
            runningTask = new Thread(new CleanTask(activity.userInput, activity.getApplicationContext(), listener));
            runningTask.start();
        }
    }

    void modifyDrawables(SelfCompileActivity activity, Intent done) {
        if (isIdle()) {
            activity.updateGui(false);
            this.done = done;
            runningTask = new Thread(
                    new ModifyDrawables(activity.userInput, activity.getApplicationContext(), listener));
            runningTask.start();
        }
    }

    void modifyStrings(SelfCompileActivity activity, Intent done) {
        if (isIdle()) {
            activity.updateGui(false);
            this.done = done;
            runningTask = new Thread(new ModifyStrings(activity.userInput, activity.getApplicationContext(), listener));
            runningTask.start();
        }
    }

    void modifyStyles(SelfCompileActivity activity, Intent done) {
        if (isIdle()) {
            activity.updateGui(false);
            this.done = done;
            runningTask = new Thread(new ModifyStyles(activity.userInput, activity.getApplicationContext(), listener));
            runningTask.start();
        }
    }

    void modifySource(SelfCompileActivity activity, Intent done) {
        if (isIdle()) {
            activity.updateGui(false);
            this.done = done;
            runningTask = new Thread(new ModifySource(activity.userInput, activity.getApplicationContext(), listener));
            runningTask.start();
        }
    }

    void startBuild(SelfCompileActivity activity, Intent done) {
        if (isIdle()) {
            activity.updateGui(false);
            this.done = done;
            runningTask = new Thread(new BuildTask(activity.userInput, activity.getApplicationContext(), listener));
            runningTask.start();
        }
    }

}

package com.duy.ide.command;

import java.util.ArrayList;

/**
 * Created by Duy on 20-Dec-17.
 */

public class Commander implements ICommand {
    private ArrayList<ICommand> commands = new ArrayList<>();

    public void addCommand(ICommand command) {
        commands.add(command);
    }

    public ArrayList<ICommand> getCommands() {
        return commands;
    }

    @Override
    public boolean execute(Object... params) {
        for (ICommand command : commands) {
            boolean execute = command.execute(params);
            if (!execute) {
                System.out.printf("Task %s executed with failed result%n", command);
                return false;
            }
        }
        System.out.println("Task" + this + " executed successful!");
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

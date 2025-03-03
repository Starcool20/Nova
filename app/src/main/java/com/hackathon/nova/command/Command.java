package com.hackathon.nova.command;

public class Command {

    public static void execute(String command) {
        final String commands = command.toLowerCase();

        if (command.startsWith("open")) {

        } else if (command.startsWith("call")) {

        } else if (command.startsWith("set alarm")) {

        } else if (command.startsWith("play")) {

        } else if (command.startsWith("send message")) {

        } else if (command.startsWith("add event")) {

        } else if (command.startsWith("go home")) {

        } else if (command.startsWith("check")) {
            performCheckOperation(command);
        } else if (command.startsWith("on")) {

        } else if (command.startsWith("off")) {

        }
    }

    private static void performCheckOperation(String command) {
        String words = command.substring(5).toLowerCase();

        switch (words) {
            case "battery percentage":

                break;
            case "storage":

                break;
            case "ram":

                break;
            case "location":

                break;
            case "wifi":

                break;
            case "internet":

                break;
            case "speaker":

                break;
            case "microphone":

                break;
            case "vibration":

                break;
            case "language":

                break;
            case "brightness":

                break;
            case "volume":

                break;
            case "weather":

                break;
            case "news":

                break;
            case "contact list":

                break;
            case "message history":

                break;
            case "notification history":

                break;
            case "bluetooth":

                break;
            default:

                break;
        }
    }
}

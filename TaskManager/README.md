# TaskManager

TaskManager is a program that runs on coprocessors (i.e. Raspberry Pi, Jetson
Nano, etc). It manages the execution and deployment of other programs that run
on the coprocessors.

## Features

  - Start tasks automatically when the robot turns on
  - Restart tasks if they end unexpectedly
  - Upload, edit, and delete task files over Messenger
  - Send tasks' standard output and error over Messenger

## Configuration

The configuration is stored in `config.json` in the current working directory.
The JSON content is structured as follows:

```
Root object
├── messengerHost (string): Hostname of the Messenger server to use
├── messengerPort (integer): Port the Messenger server is running on
├── messengerName (string): Name to identify this Messenger client with the server
├── tasksRoot (string): Name of the folder to store task files in
└── maxFailCount (integer): Maximum number of failures after which a task is cancelled
```

Tasks can either be configured over Messenger using ShuffleLog, or manually
configured in `tasks.json`, which is structured as follows:

```
Root object
└── [Task Name] (object):
    ├── workingDirectory (string): Directory the task should run in, relative to the working directory of TaskManager
    ├── command (array of string): Command to execute the task. Each argument should be split into a separate string.
    └── enabled (boolean): Whether the task is currently enabled. If it is not enabled, it will not be run.
```

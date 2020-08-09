# SIVT1

## Preparing a python virtual environment

**Requires python &geq; 3.7**

All python dependencies are stored in *requirements.txt*. To install the project and manage your dependencies, it is recommended a virtual environment is used. For more information about virtual environments, please see: [What is a virtualenv, and why should I use one?](https://stackoverflow.com/a/41972262)

### Linux and MacOS

To create a virtual environment for python in linux or MacOS via terminal run

> python3 -m venv /path/to/the/virtual/environment

The environment must be activated before you can work in it. For example, if a virtual environment was setup with,

> python3 -m venv /home/me/Envs/SIVT1

Then, in a **bash/zsh** shell, the environment can be activated with,

> source /home/me/Envs/SIVT1/bin/activate

For how to activate a virtual environment in other shells, please see the [table of activation commands for various platforms](https://docs.python.org/3/library/venv.html).

#### Setting a shortcut to activate a venv
In bash, I setup a shortcut to activate the virtual environment with the shortcut **sivt**. Open your *~/.bashrc* file, with your editor of choice, e.g.

> nano ~/.bashrc

At the end of the file append

    function shortcut_name() {
        source /path/to/the/virtual/environment/bin/activate
    }

where,

    shortcut_name = your choice of shortcut name (e.g. sivt)
    /path/to/the/virtual/environment = path to the root of the virtual environment (e.g. /home/me/Envs/SIVT/bin/activate)

Once done, save the file and exit the editor. Update your changes by running

> source ~/.bashrc

Now when you run

> sivt 

it will activate the virtual environment.

### Windows development environment

For those using windows, the most popular approach is to install [anaconda](https://www.anaconda.com/products/individual) and install PyCharm or another IDE (Integrated Development Environment), or text editor of your choosing. Please see the following links on how to setup anaconda and an IDE.

- [Detailed installation instructions](https://docs.anaconda.com/anaconda/)

- [PyCharm and Anaconda](https://docs.anaconda.com/anaconda/user-guide/tasks/pycharm/)

To create a virtual environment using anaconda, run

> conda create --name my_environment_name

To activate this environment, run

> conda activate my_environment_name

To install all dependencies, you must first have pip installed (it is **not** installed by default) using

> conda install pip

and then the requirements can be installed with

> pip install -r requirements.txt

#### Managing Dependencies

Until this process is automated in issue #21, please ensure you update your dependencies prior to merging with master.

It is important to maintain this requirements.txt file as dependencies are added and/or removed. One popular approach is to run

> pip freeze > requirements.txt

However, this returns all packages within your current python environment, which may also include unused packages. To workaround this issue, use [pipreqs](https://pypi.org/project/pipreqs/). Pipreqs can be installed by running (this works for Linux, Mac & Windows)

> pip install pipreqs

Before each commit and merge of your branch into master, run

> pipreqs /path/to/root/folder/of/project

which will automatically regenerate the requirements.txt file, and all necessary dependencies.
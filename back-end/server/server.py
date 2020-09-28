from flask import Flask, render_template, request
import os
from flask_cors import CORS
import jnius_config
from jnius import autoclass
import json


with open('./configuration/config.JSON') as config_file:
    data = json.load(config_file)

jnius_config.set_classpath('.', data['parser'])

app = Flask(__name__, root_path='../../front-end/')


@app.route('/')
def index():
    return render_template('index.html')


if __name__ == '__main__':
    app.run(debug=True)
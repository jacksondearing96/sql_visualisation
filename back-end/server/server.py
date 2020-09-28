from flask import Flask, render_template, request
import os
from flask_cors import CORS
import jnius_config
import json


with open('./configuration/config.JSON') as config_file:
    data = json.load(config_file)

jnius_config.set_classpath('.', data['lineageExtractor'])
from jnius import autoclass

app = Flask(__name__, root_path='../../front-end/')


@app.route('/')
def index():
    lineageExtractor = autoclass(data['lineageExtractorClasses'])
    response_text = lineageExtractor.extractLineageAsJson('SELECT a, b FROM c###')
    print(response_text)
    return render_template('index.html')


if __name__ == '__main__':
    app.run(debug=True)
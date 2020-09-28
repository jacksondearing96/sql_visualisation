from flask import Flask, render_template, request
import os
import jnius_config
import json

lineage_extractor_location = '../lineageExtractor/target/lineage-extractor-jar-with-dependencies.jar'
lineage_extractor_class_name = 'LineageExtractor'

jnius_config.set_classpath('.', lineage_extractor_location)
from jnius import autoclass

app = Flask(__name__, root_path='../../front-end/')


@app.route('/')
def index():
    lineageExtractor = autoclass(lineage_extractor_class_name)
    response_text = lineageExtractor.extractLineageAsJson('SELECT a, b FROM c###')
    return render_template('index.html')


if __name__ == '__main__':
    app.run(debug=True)
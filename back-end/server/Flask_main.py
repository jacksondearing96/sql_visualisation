from flask import Flask, jsonify, render_template, request
from flask_cors import CORS
import jnius_config
import json

with open('./configuration/config.JSON') as config_file:
    data = json.load(config_file)

jnius_config.set_classpath('.', data['lineageExtractor'])
from jnius import autoclass
# Configuration
DEBUG = True

app = Flask(__name__)
app.config.from_object(__name__)

# Enable CORS
CORS(app, resources={r'/*': {'origins': '*'}})


@app.route('/')
def index():
    return render_template('upload.html')


"""
Is called by the html template after file is uploaded.
The upload_file is just returning the uploaded file currently
this will be passed to the sql script in the future
which will then be redirected to the vue application.
"""

"""
Trying to integrate Java with Python Back-end

Add to your Environment Variables:
   PATH C:\Program Files\Java\jdk1.7.0_79\jre\bin\server , C:\Program Files\Java\jdk1.7.0_79\bin
   Follow instructions here: https://pyjnius.readthedocs.io/en/stable/installation.html
"""


@app.route('/uploader', methods=['GET', 'POST'])
def upload_file():
    responseObject = {'status': 'success'}
    if request.method == "POST" and request.files:
        file = request.files['file[0]']
        responseObject['input'] = request.form['concatInput']
        responseObject['name'] = file.filename
        lineageExtractor = autoclass(data['lineageExtractorClasses'])
        response_text = lineageExtractor.extractLineage(responseObject['input'])
        responseObject['tables'] = json.loads(response_text)
    return jsonify(responseObject)


if __name__ == '__main__':
    app.run(debug=True)

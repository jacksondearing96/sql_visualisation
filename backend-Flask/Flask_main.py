from flask import Flask, jsonify, render_template, request
from flask_cors import CORS
import jnius_config
import json
jnius_config.set_classpath('.', './java/sivtJavaTest.jar')
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
    response_object = {'status': 'success'}
    if request.method == "POST" and request.files:
        file = request.files['file']
        response_object['name'] = file.filename
        agentLeadOutput = autoclass('au.com.gabriel.sivtjavatest.scriptOutputTest')
        sb = agentLeadOutput()
        responseText = sb.outputDataStruct(file.filename)
        response_object['tables'] = json.loads(responseText)
    return jsonify(response_object)


if __name__ == '__main__':
    app.run(debug=True)

from flask import Flask, jsonify, render_template, request
from flask_cors import CORS

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


@app.route('/uploader', methods=['GET', 'POST'])
def upload_file():
    response_object = {'status': 'success'}
    if request.method == "POST" and request.files:
        file = request.files['file']
        response_object['name'] = file.filename
    return jsonify(response_object)


if __name__ == '__main__':
    app.run(debug=True)

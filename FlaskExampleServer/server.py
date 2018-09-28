from flask import Flask
from flask import jsonify
from flask import request
from flask import Response
from flask import send_file
import os

app = Flask(__name__)
app.config['SERVER_NAME'] = 'apphire.memleak.pl:5000'

UPLOAD_FOLDER = '/home/wigdis/results/'
EXP_FOLDER = '/home/wigdis/exp/'
RESULT = 'result'
EXT = '.csv'
RESPONSE = 'File uploaded successfully'

counter = 0


@app.route('/exp/', methods=['GET'])
def list_experiments():
    exp = list()
    files = os.listdir(EXP_FOLDER)
    for file in files:
        exp.append(file)

    return jsonify(exp)


@app.route('/exp/<string:exp>', methods=['GET'])
def get_experiment(exp):
    return send_file(EXP_FOLDER + exp)


@app.route('/results/', methods=['POST'])
def result():
    global counter
    counter += 1

    f = request.files['file']
    f.save(UPLOAD_FOLDER + RESULT + str(counter) + EXT)

    return Response(RESPONSE, status=200)


if __name__ == '__main__':
    app.run(host= '0.0.0.0', port=5000)

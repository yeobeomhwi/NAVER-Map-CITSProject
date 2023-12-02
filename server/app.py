from flask import Flask, jsonify, request
import sqlite3
import requests

map_api_url = "http://t-data.seoul.go.kr/apig/apiman-gateway/tapi/v2xCrossroadMapInformation/1.0"
traffic_api_url = "http://t-data.seoul.go.kr/apig/apiman-gateway/tapi/v2xSignalPhaseTimingInformation/1.0"
api_key = "23c019e8-2837-440c-b368-1a5e6a1882ba"

app = Flask(__name__)

def get_traffic(itstId) :
    traffic_parameter = {
            "type" : "json",
            "pageNo" : "1",
            "numOfRows" : "1",
            "apiKey" : api_key,
            "itstId" : str(itstId)
    }
    req = requests.get(traffic_api_url, params=traffic_parameter)
    data = req.json()
    try :
        traffic_data = (data[0])
        return_data = []
        None_headers = ['dataId', 'trsmUtcTime', 'trsmYear', 'trsmMt', 'trsmDy', 'trsmTm', 'trsmMs', 'itstId', 'eqmnId', 'msgCreatMin', 'msgCreatDs', 'rgtrId', 'regDt']
        for key, value in traffic_data.items() :
            if value != None and key not in None_headers :
                return_data.append(value)
        return_data = list(set(return_data))
    except :
        return_data = None
    print(return_data)
    return return_data

@app.route('/get_lat_lng', methods=['POST'])
def get_lat_lot():
    conn = sqlite3.connect('map.db')
    cursor = conn.cursor()
    x = request.args.get('x')[:7]  # x에 해당하는 값 얻기
    y = request.args.get('y')[:8]
    print(x, y)
    cursor.execute("SELECT itstId, Lat, Lot FROM my_table WHERE Lat LIKE ? OR Lot LIKE ?", ('%' + x + '%', '%' + y + '%'))
    result = cursor.fetchall()
    print(result)
    x = result[-1][1]
    y = result[-1][2]
    itstId = result[-1][0]
    try :
        traffic = get_traffic(itstId)[0]
    except :
        traffic = 0
    print(x, y, traffic)
    
    response_data = {
        'x': str(x),
        'y': str(y),
        'traffic': str(traffic)
    }
    
    return jsonify(response_data)
    # return jsonify({
    #     'x' : str(x)
    # })
    # return str(x), str(y), str(traffic)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=6000)

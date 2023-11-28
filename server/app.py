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
    return return_data

@app.route('/get_lat_lot', methods=['POST'])
def get_lat_lot():
    conn = sqlite3.connect('map.db')
    cursor = conn.cursor()
    data = request.get_json()
    x = str(data['x'])
    y = str(data['y'])
    cursor.execute("SELECT itstId, Lat, Lot FROM my_table WHERE Lat LIKE ? OR Lot LIKE ?", ('%' + x + '%', '%' + y + '%'))
    result = cursor.fetchall()
    datas = []
    for i in result :
        itstId = i[0]
        Lat = i[1]
        Lot = i[2]
        traffic_data = get_traffic(itstId)
        datas.append([Lat, Lot, traffic_data])
    return jsonify(datas)


if __name__ == '__main__':
    app.run(debug=True)

<template>
  <div class="app-container1">
    <div class="filter-container">
      <el-input v-model="listQuery.deviceName" placeholder="设备名" class="filter-item device-name-input" @keyup.enter.native="handleLocate" />
      <el-button v-waves class="filter-item locate-device" type="primary" icon="el-icon-location" @click="handleLocate">
        定位设备
      </el-button>
      <el-button v-waves class="filter-item view-history" type="primary" icon="el-icon-search" @click="viewRoute">
        查看历史轨迹
      </el-button>
      <el-autocomplete
          v-model="form.address"
          class="location-info"
          popper-class="autoAddressClass"
          :fetch-suggestions="querySearchAsync"
          :trigger-on-focus="false"
          placeholder="位置信息"
          clearable
          @select="handleSelect"
        >
          <template slot-scope="{ item }">
            <i class="el-icon-search fl mgr10" />
            <div style="overflow:hidden;">
              <div class="title">{{ item.title }}</div>
              <span class="address ellipsis">{{ item.address }}</span>
            </div>
          </template>
        </el-autocomplete>
    </div>
    <div id="map-container" class="map" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import loadBMap from '@/utils/loadBMap.js'
import { Message } from 'element-ui'
import { getRoute } from '@/api/device'

export default {
  name: 'Map',
  computed: {
    ...mapGetters([
      'deviceList'
    ])
  },
  data() {
    return {
      listQuery: {
        deviceName: ''
      },
      list: null,
      form: {
        address: '', // 详细地址
        addrPoint: { // 详细地址经纬度
          lng: 0,
          lat: 0
        }
      },
      map: '', // 地图实例
      mk: '', // 当前位置Marker实例
      mk1: '', // 设备位置Marker实例
      locationPoint: null
    }
  },
  async mounted() {
    await loadBMap('QEE0RBhjT3DefFvR1sC8rddzUmLREpNK') // 加载引入BMap
    this.initMap()
  },
  methods: {
    // 初始化地图
    initMap() {
      var that = this

      var current_location_icon = new BMap.Icon(
        require("@/assets/current_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      var online_location_icon = new BMap.Icon(
        require("@/assets/online_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      var alert_location_icon = new BMap.Icon(
        require("@/assets/alert_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      // 挂载地图
      this.map = new BMap.Map('map-container', { enableMapClick: false })
      var point = new BMap.Point(120.12931031715365, 30.270238619316196)
      this.map.centerAndZoom(point, 15)

      // 显示所有设备位置信息
      for (let i = 0; i < this.deviceList.length; i++) {
        var point1 = new BMap.Point(this.deviceList[i].longitude, this.deviceList[i].latitude)
        var opts = {
          position: point1,    // 指定文本标注所在的地理位置
          width: 100,     // 信息窗口宽度
          height: 50,     // 信息窗口高度
          offset: new BMap.Size(0, -20), //设置文本偏移量
        }
        var content = '设备名: ' + this.deviceList[i].name + '<br>' 
                    + '设备类型: ' + this.deviceList[i].type + '<br>'
                    + '设备数据: ' + this.deviceList[i].value + '<br>';

        var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
        var marker
        if (this.deviceList[i].state == 1) {
          marker = new BMap.Marker(point1, { icon: online_location_icon})
        } else {
          marker = new BMap.Marker(point1, { icon: alert_location_icon})
        }
        marker.infoWindow = infoWindow
        marker.addEventListener("click", (function(marker) {
          return function() {
            this.map.openInfoWindow(marker.infoWindow, marker.getPosition()); // 开启信息窗口
          }
        })(marker));
        this.map.addOverlay(marker)
      }

      // 设置图像标注并绑定拖拽标注结束后事件
      this.mk = new BMap.Marker(point, { icon: current_location_icon, enableDragging: true })
      this.map.addOverlay(this.mk)
      this.mk.addEventListener('dragend', function(e) {
        that.getAddrByPoint(e.point)
      })

      // 添加（右上角）平移缩放控件
      this.map.addControl(new BMap.NavigationControl({ anchor: BMAP_ANCHOR_TOP_RIGHT }))

      // 添加（右上角）比例尺控件
      this.map.addControl(new BMap.ScaleControl({ anchor: BMAP_ANCHOR_TOP_RIGHT }))

      // 添加（左上角）地图类型控件
      this.map.addControl(new BMap.MapTypeControl({ mapTypes: [BMAP_NORMAL_MAP, BMAP_HYBRID_MAP], anchor: BMAP_ANCHOR_TOP_LEFT }))

      // 添加（右下角）缩略图控件
      this.map.addControl(new BMap.OverviewMapControl({ anchor: BMAP_ANCHOR_BOTTOM_RIGHT, isOpen: true }))

      // 添加（左下角）定位控件
      var geolocationControl = new BMap.GeolocationControl({ anchor: BMAP_ANCHOR_BOTTOM_LEFT })
      geolocationControl.addEventListener('locationSuccess', function(e) {
        that.getAddrByPoint(e.point)
      })
      geolocationControl.addEventListener('locationError', function(e) {
        alert(e.message)
      })
      this.map.addControl(geolocationControl)

      // 浏览器定位
      this.geolocation()

      // 绑定点击地图任意点事件
      this.map.addEventListener('click', function(e) {
        that.getAddrByPoint(e.point)
      })
    },
    // 获取两点间的距离
    getDistance(pointA, pointB) {
      return this.map.getDistance(pointA, pointB).toFixed(2)
    },
    // 浏览器定位函数
    geolocation() {
      var that = this
      var geolocation = new BMap.Geolocation()
      geolocation.getCurrentPosition(function(res) {
        if (this.getStatus() == BMAP_STATUS_SUCCESS) {
          that.getAddrByPoint(res.point)
          that.locationPoint = res.point
        } else {
          that.locationPoint = new BMap.Point(120.12931031715365, 30.270238619316196)
        }
      }, { enableHighAccuracy: true })
    },
    // 逆地址解析函数
    getAddrByPoint(point) {
      var that = this
      var geco = new BMap.Geocoder()
      geco.getLocation(point, function(res) {
        that.mk.setPosition(point)
        that.map.panTo(point)
        that.form.address = res.address
        that.form.addrPoint = point
      })
    },
    querySearchAsync(str, cb) {
      var options = {
        onSearchComplete: function(res) {
          var s = []
          if (local.getStatus() == BMAP_STATUS_SUCCESS) {
            for (var i = 0; i < res.getCurrentNumPois(); i++) {
              s.push(res.getPoi(i))
            }
            cb(s)
          } else {
            cb(s)
          }
        }
      }
      var local = new BMap.LocalSearch(this.map, options)
      local.search(str)
    },
    handleSelect(item) {
      var current_location_icon = new BMap.Icon(
        require("@/assets/current_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      var online_location_icon = new BMap.Icon(
        require("@/assets/online_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      var alert_location_icon = new BMap.Icon(
        require("@/assets/alert_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      this.form.address = item.address + item.title
      this.form.addrPoint = item.point
      this.map.clearOverlays()
      this.mk = new BMap.Marker(item.point, { icon: current_location_icon})
      this.map.addOverlay(this.mk)
      // 显示所有设备位置信息
      for (let i = 0; i < this.deviceList.length; i++) {
        var point1 = new BMap.Point(this.deviceList[i].longitude, this.deviceList[i].latitude)
        var opts = {
          position: point1,    // 指定文本标注所在的地理位置
          width: 100,     // 信息窗口宽度
          height: 50,     // 信息窗口高度
          offset: new BMap.Size(0, -20), //设置文本偏移量
        }
        var content = '设备名: ' + this.deviceList[i].name + '<br>' 
                    + '设备类型: ' + this.deviceList[i].type + '<br>'
                    + '设备数据: ' + this.deviceList[i].value + '<br>';

        var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
        var marker
        if (this.deviceList[i].state == 1) {
          marker = new BMap.Marker(point1, { icon: online_location_icon})
        } else {
          marker = new BMap.Marker(point1, { icon: alert_location_icon})
        }
        marker.infoWindow = infoWindow
        marker.addEventListener("click", (function(marker) {
          return function() {
            this.map.openInfoWindow(marker.infoWindow, marker.getPosition()); // 开启信息窗口
          }
        })(marker));
        this.map.addOverlay(marker)
      }
      this.map.panTo(item.point)
    },
    handleLocate() {
      var online_location_icon = new BMap.Icon(
        require("@/assets/online_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      var alert_location_icon = new BMap.Icon(
        require("@/assets/alert_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      this.map.clearOverlays()
      this.map.addOverlay(this.mk)
      // 显示所有设备位置信息
      for (let i = 0; i < this.deviceList.length; i++) {
        var point1 = new BMap.Point(this.deviceList[i].longitude, this.deviceList[i].latitude)
        var opts = {
          position: point1,    // 指定文本标注所在的地理位置
          width: 100,     // 信息窗口宽度
          height: 50,     // 信息窗口高度
          offset: new BMap.Size(0, -20), //设置文本偏移量
        }
        var content = '设备名: ' + this.deviceList[i].name + '<br>' 
                    + '设备类型: ' + this.deviceList[i].type + '<br>'
                    + '设备数据: ' + this.deviceList[i].value + '<br>';

        var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
        var marker
        if (this.deviceList[i].state == 1) {
          marker = new BMap.Marker(point1, { icon: online_location_icon})
        } else {
          marker = new BMap.Marker(point1, { icon: alert_location_icon})
        }
        marker.infoWindow = infoWindow
        marker.addEventListener("click", (function(marker) {
          return function() {
            this.map.openInfoWindow(marker.infoWindow, marker.getPosition()); // 开启信息窗口
          }
        })(marker));
        this.map.addOverlay(marker)
      }
      // 根据设备名查找设备位置
      var idx = -1;
      for (let i = 0; i < this.deviceList.length; i++) {
        if (this.deviceList[i].name == this.listQuery.deviceName) {
          idx = i;
          break;
        }
      }
      if (idx == -1) {
        // 处理找不到标记的情况
        Message.error('该设备不存在');
        return;
      }
      // 遍历地图上的覆盖物，查找匹配的标记
      var targetMarker = null;
      for (let i = 0; i < this.map.getOverlays().length; i++) {
        var overlay = this.map.getOverlays()[i];
        // 检查覆盖物是否为标记
        if (overlay instanceof BMap.Marker) {
          var position = overlay.getPosition();
          // 检查标记的经纬度是否匹配目标位置
          if (position.lng === this.deviceList[idx].longitude && position.lat === this.deviceList[idx].latitude) {
            targetMarker = overlay;
            break;
          }
        }
      }
      // 如果找到匹配的标记，则打开其信息窗口
      if (targetMarker) {
        this.map.openInfoWindow(targetMarker.infoWindow, targetMarker.getPosition());
        this.map.panTo(targetMarker.getPosition());
      } else {
        // 处理找不到标记的情况
        Message.error('该设备不存在');
      }
    },
    viewRoute() {
      var online_location_icon = new BMap.Icon(
        require("@/assets/online_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      var alert_location_icon = new BMap.Icon(
        require("@/assets/alert_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      this.map.clearOverlays()
      this.map.addOverlay(this.mk)
      // 显示所有设备位置信息
      for (let i = 0; i < this.deviceList.length; i++) {
        var point1 = new BMap.Point(this.deviceList[i].longitude, this.deviceList[i].latitude)
        var opts = {
          position: point1,    // 指定文本标注所在的地理位置
          width: 100,     // 信息窗口宽度
          height: 50,     // 信息窗口高度
          offset: new BMap.Size(0, -20), //设置文本偏移量
        }
        var content = '设备名: ' + this.deviceList[i].name + '<br>' 
                    + '设备类型: ' + this.deviceList[i].type + '<br>'
                    + '设备数据: ' + this.deviceList[i].value + '<br>';

        var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
        var marker
        if (this.deviceList[i].state == 1) {
          marker = new BMap.Marker(point1, { icon: online_location_icon})
        } else {
          marker = new BMap.Marker(point1, { icon: alert_location_icon})
        }
        marker.infoWindow = infoWindow
        marker.addEventListener("click", (function(marker) {
          return function() {
            this.map.openInfoWindow(marker.infoWindow, marker.getPosition()); // 开启信息窗口
          }
        })(marker));
        this.map.addOverlay(marker)
      }

      var previous_location_icon = new BMap.Icon(
        require("@/assets/previous_location.png"),
        new BMap.Size(40, 40),
        {
          imageSize: new BMap.Size(28,28),
          imageOffset:new BMap.Size(0,0) 
        }
      )

      // 根据设备名查找设备位置
      var idx = -1;
      for (let i = 0; i < this.deviceList.length; i++) {
        if (this.deviceList[i].name == this.listQuery.deviceName) {
          idx = i;
          break;
        }
      }
      if (idx == -1) {
        // 处理找不到标记的情况
        Message.error('该设备不存在');
        return;
      }
      // 遍历地图上的覆盖物，查找匹配的标记
      var targetMarker = null;
      for (let i = 0; i < this.map.getOverlays().length; i++) {
        var overlay = this.map.getOverlays()[i];
        // 检查覆盖物是否为标记
        if (overlay instanceof BMap.Marker) {
          var position = overlay.getPosition();
          // 检查标记的经纬度是否匹配目标位置
          if (position.lng === this.deviceList[idx].longitude && position.lat === this.deviceList[idx].latitude) {
            targetMarker = overlay;
            break;
          }
        }
      }
      // 如果找到匹配的标记，则打开其信息窗口
      if (targetMarker) {
        this.map.openInfoWindow(targetMarker.infoWindow, targetMarker.getPosition());
        this.map.panTo(targetMarker.getPosition());
      } else {
        // 处理找不到标记的情况
        Message.error('该设备不存在');
      }

      getRoute(this.listQuery.deviceName).then((response) => {
        this.list = response.data.items;
        var new_list = [];
        for (let i = 0; i < this.list.length - 1; i++) {
          while (i < this.list.length - 1 && this.list[i].longitude == this.list[i + 1].longitude && this.list[i].latitude == this.list[i + 1].latitude) {
            i++;
          }
          if (i == this.list.length - 1) {
            break;
          }
          new_list.push(this.list[i]);
        }
        new_list.push(this.list[this.list.length - 1]);
        this.list = new_list;

        if (this.list.length > 1) {
          var point1 = new BMap.Point(this.list[0].longitude, this.list[0].latitude);
          var opts = {
            position: point1,    // 指定文本标注所在的地理位置
            width: 100,     // 信息窗口宽度
            height: 50,     // 信息窗口高度
            offset: new BMap.Size(0, -20), //设置文本偏移量
          }
          var content
          if (this.list[0].alert == 1) {
            content = '设备状态: 报警' + '<br>'
                      + '设备数据: ' + this.list[0].value + '<br>'
                      + '时间: ' + this.list[0].time;
          } else {
            content = '设备状态: 正常' + '<br>'
                      + '设备数据: ' + this.list[0].value + '<br>'
                      + '时间: ' + this.list[0].time;
          }
          var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
          var marker = new BMap.Marker(point1, { icon: previous_location_icon})
          marker.infoWindow = infoWindow
          marker.addEventListener("click", (function(marker) {
            return function() {
              this.map.openInfoWindow(marker.infoWindow, marker.getPosition()); // 开启信息窗口
            }
          })(marker));
          this.map.addOverlay(marker)
        }

        var point2;
        for (let i = 1; i < this.list.length - 1; i++) {
          point2 = new BMap.Point(this.list[i].longitude, this.list[i].latitude);
          opts = {
            position: point2,    // 指定文本标注所在的地理位置
            width: 100,     // 信息窗口宽度
            height: 50,     // 信息窗口高度
            offset: new BMap.Size(0, -20), //设置文本偏移量
          }
          if (this.list[i].alert == 1) {
            content = '设备状态: 报警' + '<br>'
                      + '设备数据: ' + this.list[i].value + '<br>'
                      + '时间: ' + this.list[i].time;
          } else {
            content = '设备状态: 正常' + '<br>'
                      + '设备数据: ' + this.list[i].value + '<br>'
                      + '时间: ' + this.list[i].time;
          }
          infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
          marker = new BMap.Marker(point2, { icon: previous_location_icon});
          marker.infoWindow = infoWindow
          marker.addEventListener("click", (function(marker) {
            return function() {
              this.map.openInfoWindow(marker.infoWindow, marker.getPosition()); // 开启信息窗口
            }
          })(marker));
          this.map.addOverlay(marker);
          this.drawLine(point1, point2);
          point1 = point2;
        }
        if (this.list.length > 1) {
          point2 = new BMap.Point(this.deviceList[idx].longitude, this.deviceList[idx].latitude);
          this.drawLine(point1, point2);
        }
      })
    },
    drawLine (p1, p2) {
      var mid = new BMap.Point((p1.lng + p2.lng) / 2, (p1.lat + p2.lat) / 2);
      var opts = {
        position: mid,    // 指定文本标注所在的地理位置
        width: 100,     // 信息窗口宽度
        height: 50,     // 信息窗口高度
        offset: new BMap.Size(0, -20), //设置文本偏移量
      }
      var content = '总距离: ' + this.getDistance(p1, p2) + ' 米' + '<br>' + 
                    '起点: ( ' + parseFloat(p1.lng).toFixed(4) + ', ' + parseFloat(p1.lat).toFixed(4) + ' )' + '<br>' + 
                    '终点: ( ' + parseFloat(p2.lng).toFixed(4) + ', ' + parseFloat(p2.lat).toFixed(4) + ' )';
      var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象
      var line = new BMap.Polyline([
        p1,
        p2
      ], {strokeColor:"blue", strokeWeight: 5, strokeOpacity: 0.5});
      line.infoWindow = infoWindow
      line.addEventListener("click", (function(line) {
        return function() {
          this.map.openInfoWindow(line.infoWindow, mid); // 开启信息窗口
        }
      })(line));
      this.map.addOverlay(line);
    }
  }
}
</script>

<style lang="scss" scoped>
.map {
  margin-top: 15px;
  width: 100%;
  height: 86vh;
}

.app-container1 {
  padding: 10px;
}

.autoAddressClass{
  li {
    i.el-icon-search {margin-top:11px;}
    .mgr10 {margin-right: 10px;}
    .title {
      text-overflow: ellipsis;
      overflow: hidden;
    }
    .address {
      line-height: 1;
      font-size: 12px;
      color: #b4b4b4;
      margin-bottom: 5px;
    }
  }
}

.device-name-input {
  width: 140px;
  @media (max-width: 550px) {
    width: 73px;
  }
}

.locate-device {
  margin-left: 10px;
  margin-top: 6px;
  @media (max-width: 550px) {
    margin-left: 0px;
    margin-top: 6px;
    width: 117px;
  }
}

.view-history {
  margin-left: 10px;
  margin-top: 6px;
  @media (max-width: 550px) {
    margin-left: 0px;
    margin-top: 6px;
    width: 150px;
  }
}

.location-info {
  width:35%;
  margin-left:50px;
  @media (max-width: 550px) {
    width:100%;
    margin-left:0px;
    margin-top: 10px;
  }
}

</style>

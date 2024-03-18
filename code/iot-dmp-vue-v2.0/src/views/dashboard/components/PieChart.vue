<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'
import { mapGetters } from 'vuex'

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '386px'
    }
  },
  computed: {
    ...mapGetters([
      'sensorNum',
      'actuatorNum',
      'embeddedNum',
      'communicationNum',
      'controllerNum'
    ])
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')

      // 获取屏幕宽度
      const screenWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;

      // 设置radius值，根据屏幕宽度调整
      const radius = screenWidth <= 550 ? [10, 80] : [20, 120];

      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c} ({d}%)'
        },
        legend: {
          left: 'center',
          bottom: '30',
          data: ['传感器设备', '执行器设备', '嵌入式设备', '通讯设备', '控制器设备']
        },
        series: [
          {
            name: '设备类型统计',
            type: 'pie',
            roseType: 'radius',
            radius: radius,
            center: ['50%', '40%'],
            data: [
              { value: this.sensorNum, name: '传感器设备' },
              { value: this.actuatorNum, name: '执行器设备' },
              { value: this.embeddedNum, name: '嵌入式设备' },
              { value: this.communicationNum, name: '通讯设备' },
              { value: this.controllerNum, name: '控制器设备' }
            ],
            animationEasing: 'cubicInOut',
            animationDuration: 1000
          }
        ]
      })
    }
  }
}
</script>

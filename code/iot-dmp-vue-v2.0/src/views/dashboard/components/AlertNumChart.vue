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
      default: '350px'
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      }
    }
  },
  computed: {
    ...mapGetters([
      'hourList',
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
      this.setOptions(this.chartData)
    },
    setOptions({ data } = {}) {
      this.chart.setOption({
        xAxis: {
          data: this.hourList,
          boundaryGap: false,
          axisTick: {
            show: false,
            length: 10
          },
          axisLabel: {
            show: true,  // 显示刻度标签
            margin: 15    // 与边界的间隔
          }
        },
        grid: {
          left: 18,
          right: 32,
          bottom: 20,
          top: 30,
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          padding: [5, 10]
        },
        yAxis: {
          axisTick: {
            show: false
          },
          splitLine: {
            show: false
          }
        },
        legend: {
          data: ['报警数量']
        },
        series: [{
          name: '报警数量',
          smooth: true,
          type: 'line',
          itemStyle: {
            normal: {
              color: '#f4516c',
              lineStyle: {
                color: '#f4516c',
                width: 2
              },
              areaStyle: {
                color:  '#e2c9cd'
              }
            }
          },
          data: data,
          animationDuration: 1000,
          animationEasing: 'quadraticOut'
        }]
      })
    }
  }
}
</script>

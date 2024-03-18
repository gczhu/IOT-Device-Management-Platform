<template>
  <div class="dashboard-editor-container">

    <panel-group />

    <div>
      <el-row style="display: flex; justify-content: space-between;">
        <el-row class="statistic">
          <device-num-chart :chart-data="deviceNumData" />
        </el-row>
      </el-row>
    </div>

    <div>
      <el-row style="display: flex; justify-content: space-between;">
        <el-row class="statistic">
          <m-q-t-t-msg-num-chart :chart-data="MQTTMsgNumData" />
        </el-row>
      </el-row>
    </div>

    <div>
      <el-row style="display: flex; justify-content: space-between;">
        <el-row class="statistic">
          <online-num-chart :chart-data="onlineNumData" />
        </el-row>
      </el-row>
    </div>

    <div>
      <el-row style="display: flex; justify-content: space-between;">
        <el-row class="statistic">
          <alert-num-chart :chart-data="alertNumData" />
        </el-row>
      </el-row>
    </div>

    <el-row :gutter="32">
      <el-col :xs="{span: 24}" :sm="{span: 24}" :md="{span: 24}" :lg="{span: 14}" :xl="{span: 14}" style="padding-right:8px;margin-bottom:30px;">
        <transaction-table />
      </el-col>
      <el-col :xs="{ span: 24 }" :sm="{ span: 24 }" :lg="{ span: 10 }">
        <div class="chart-wrapper">
          <pie-chart />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import PanelGroup from './components/PanelGroup'
import DeviceNumChart from './components/DeviceNumChart'
import OnlineNumChart from './components/OnlineNumChart'
import AlertNumChart from './components/AlertNumChart'
import MQTTMsgNumChart from './components/MQTTMsgNumChart'
import PieChart from './components/PieChart'
import TransactionTable from './components/TransactionTable'
import { mapGetters } from 'vuex'

export default {
  name: 'DashboardAdmin',
  components: {
    PanelGroup,
    DeviceNumChart,
    OnlineNumChart,
    AlertNumChart,
    MQTTMsgNumChart,
    PieChart,
    TransactionTable
  },
  computed: {
    ...mapGetters([
      'deviceNumList',
      'onlineNumList',
      'alertNumList',
      'MQTTMsgNumList'
    ])
  },
  data() {
    return {
      deviceNumData: {
        data: []
      },
      onlineNumData: {
        data: []
      },
      alertNumData: {
        data: []
      },
      MQTTMsgNumData: {
        data: []
      }
    }
  },
  mounted() {
    this.deviceNumData.data = this.deviceNumList
    this.onlineNumData.data = this.onlineNumList
    this.alertNumData.data = this.alertNumList
    this.MQTTMsgNumData.data = this.MQTTMsgNumList
    this.refreshInterval = setInterval(() => {
      this.refreshPage();
    }, 60000);
  },
  beforeDestroy() {
    clearInterval(this.refreshInterval);
  },
  methods: {
    refreshPage() {
      window.location.reload();
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }

  .statistic {
    background:#fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
    width: 100%
  }

}

@media (max-width:1024px) {
  .dashboard-editor-container {
    padding: 8px;

    .chart-wrapper {
      padding: 8px;
    }
  }
}
</style>

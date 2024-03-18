<template>
  <el-table :data="list" style="width: 100%;padding-top: 15px;">
    <el-table-column label="设备ID" min-width="70" align="center">
      <template slot-scope="scope">
        {{ scope.row.device_id }}
      </template>
    </el-table-column>
    <el-table-column label="消息内容" min-width="200" align="center">
      <template slot-scope="scope">
        {{ scope.row.info }}
      </template>
    </el-table-column>
    <el-table-column label="数据" min-width="50" align="center">
      <template slot-scope="scope">
        {{ scope.row.value }}
      </template>
    </el-table-column>
    <el-table-column label="经度" min-width="85" align="center">
      <template slot-scope="scope">
        {{ parseFloat(scope.row.longitude).toFixed(4) }}
      </template>
    </el-table-column>
    <el-table-column label="纬度" min-width="85" align="center">
      <template slot-scope="scope">
        {{ parseFloat(scope.row.latitude).toFixed(4) }}
      </template>
    </el-table-column>
    <el-table-column label="时间" width="160" align="center">
      <template slot-scope="scope">
        {{ scope.row.time}}
      </template>
    </el-table-column>
    <el-table-column label="状态" width="90" align="center">
      <template slot-scope="{row}">
        <el-tag :type="row.alert | statusFilter">
          {{ row.alert | statusText }}
        </el-tag>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  filters: {
    statusFilter(status) {
      const statusMap = {
        0: 'success',
        1: 'danger'
      }
      return statusMap[status]
    },
    statusText(value) {
      return value === 0 ? '在线' : '报警'; // 0 显示为 '在线'，1 显示为 '报警'
    }
  },
  data() {
    return {
      list: null
    }
  },
  computed: {
    ...mapGetters([
      'MQTTMsgList'
    ])
  },
  created() {
    this.list = this.MQTTMsgList.slice(0, 6)
  }
}
</script>

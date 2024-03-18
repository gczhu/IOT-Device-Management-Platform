<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.deviceName" placeholder="设备名" style="width: 140px;" class="filter-item" @keyup.enter.native="handleFilter" />
      <el-select v-model="listQuery.state" placeholder="状态" clearable style="width: 140px" class="filter-item">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.label" :value="item.key" />
      </el-select>
      <el-date-picker v-model="listQuery.startDate" type="date" placeholder="开始日期" style="width: 140px;" class="filter-item" />
      <el-date-picker v-model="listQuery.endDate" type="date" placeholder="结束日期" style="width: 140px;" class="filter-item" />
      <el-button v-waves class="filter-item search" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
    </div>

    <el-table
      :key="tableKey"
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%; margin-top:20px"
    >
      <el-table-column label="消息ID" prop="id" align="center" width="100px">
        <template slot-scope="{row}">
          <span>{{ row.msg_id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="设备名" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.device_name }}</span>
        </template>
      </el-table-column>

      <el-table-column label="消息内容" min-width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.info }}</span>
        </template>
      </el-table-column>

      <el-table-column label="设备数据" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.value }}</span>
        </template>
      </el-table-column>

      <el-table-column label="地理位置" width="160px" align="center">
        <template slot-scope="{row}">
                    <span>
            ({{ parseFloat(row.longitude).toFixed(4) }}, 
            {{ parseFloat(row.latitude).toFixed(4) }})
          </span>
        </template>
      </el-table-column>

      <el-table-column label="上报时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.time | parseTime('{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" class-name="status-col" width="90px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.alert | statusFilter">
            {{ row.alert | statusText }}
          </el-tag>
        </template>
      </el-table-column>

    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

  </div>
</template>

<script>
import waves from '@/directive/waves' // waves directive
import Pagination from '@/components/Pagination' // secondary package based on el-pagination
import { mapGetters } from 'vuex'
import { searchMessage } from '@/api/message'

export default {
  name: 'LogTable',
  components: { Pagination },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        0: 'success',
        1: 'danger'
      }
      return statusMap[status]
    },
    statusText(value) {
      return value === 1 ? '报警' : '在线'; // 0 显示为 '在线'，1 显示为 '报警'
    }
  },
  data() {
    return {
      tableKey: 0,
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        username: '',
        deviceName: '',
        state: '',
        startDate: '',
        endDate: ''
      },
      statusOptions: [{ label: '在线', key: '0' }, { label: '报警', key: '1' }],
      customLabelWidth: '80px',
    }
  },
  computed: {
    ...mapGetters([
      'username'
    ])
  },
  created() {
    this.listQuery.username = this.username
    this.getList()
  },
  mounted() {
    this.refreshInterval = setInterval(() => {
      this.refreshPage();
    }, 60000);
  },
  methods: {
    getList() {
      this.listLoading = true
      searchMessage(this.listQuery).then((response) => {
        this.list = response.data.items
        this.total = response.data.total
      })
      // Just to simulate the time of the request
      setTimeout(() => {
        this.listLoading = false
      }, 400)
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    refreshPage() {
      window.location.reload();
    }
  }
}
</script>

<style lang="scss" scoped>
.filter-item {
  @media (max-width: 550px) {
    width: 160px !important;
  }
}

.search {
  margin-left: 10px;
  width: 120px;
  @media (max-width: 550px) {
    margin-left: 0px;
    margin-top: 10px;
  }
}
</style>
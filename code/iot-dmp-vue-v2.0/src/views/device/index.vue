<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.id" placeholder="设备ID" style="width: 140px;" class="filter-item" @keyup.enter.native="handleFilter" />
      <el-input v-model="listQuery.name" placeholder="设备名" style="width: 140px;" class="filter-item" @keyup.enter.native="handleFilter" />
      <el-select v-model="listQuery.type" placeholder="设备类型" clearable class="filter-item" style="width: 140px">
        <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="listQuery.state" placeholder="设备状态" clearable style="width: 140px" class="filter-item">
        <el-option v-for="item in statusOptions" :key="item.key" :label="item.label" :value="item.key" />
      </el-select>
      <el-date-picker v-model="listQuery.startDate" type="date" placeholder="开始日期" style="width: 140px;" class="filter-item" />
      <el-date-picker v-model="listQuery.endDate" type="date" placeholder="结束日期" style="width: 140px;" class="filter-item" />
      <el-select v-model="listQuery.sort" class="filter-item sort" @change="handleFilter">
        <el-option v-for="item in sortOptions" :key="item.key" :label="item.label" :value="item.key" />
      </el-select>
      <el-button v-waves class="filter-item search-device" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索设备
      </el-button>
      <el-button class="filter-item create-device" type="primary" icon="el-icon-plus" @click="handleCreate">
        创建设备
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
      @sort-change="sortChange"
    >
      <el-table-column label="设备ID" prop="id" sortable="custom" align="center" width="100px" :class-name="getSortClass('id')">
        <template slot-scope="{row}">
          <span>{{ row.device_id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="设备名" width="120px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column label="设备类型" width="120px" align="center">
        <template slot-scope="{row}">
          <el-tag>{{ row.type }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="设备描述" min-width="100px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.description }}</span>
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

      <el-table-column label="创建时间" width="160px" align="center">
        <template slot-scope="{row}">
          <span>{{ row.created_time | parseTime('{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" class-name="status-col" width="90px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.state | statusFilter">
            {{ row.state | statusText }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template slot-scope="{row,$index}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">
            编辑
          </el-button>
          <el-button v-if="row.status!='deleted'" size="mini" type="danger" @click="handleDelete(row,$index)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible" :fullscreen="isMobile">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="70px" class="table">
        <el-form-item label="设备名" prop="name" :label-width="customLabelWidth">
          <el-input v-model="temp.name" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type" :label-width="customLabelWidth">
          <el-select v-model="temp.type" class="filter-item" placeholder="请选择">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备描述" prop="description" :label-width="customLabelWidth">
          <el-input v-model="temp.description" :autosize="{ minRows: 2, maxRows: 5}" type="textarea" placeholder="请填写设备描述" class="description" />
        </el-form-item>
      </el-form>
      <div v-if="showDataStatistic">
        <el-row style="display: flex; justify-content: space-between;">
          <el-row class="statistic">
            <data-statistic-chart :chart-data="valueData" />
          </el-row>
        </el-row>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogStatus==='create'?createData():updateData()">
          确认
        </el-button>
        <el-button @click="dialogFormVisible = false">
          取消
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import waves from '@/directive/waves' // waves directive
import Pagination from '@/components/Pagination' // secondary package based on el-pagination
import { mapGetters } from 'vuex'
import { createDevice, deleteDevice, modifyDevice, searchDevice, getDataList } from '@/api/device'
import DataStatisticChart from './components/DataStatisticChart.vue'

const typeOptions = ['传感器设备', '执行器设备', '嵌入式设备', '通讯设备', '控制器设备']

export default {
  name: 'DeviceTable',
  components: { 
    Pagination,
    DataStatisticChart
  },
  directives: { waves },
  filters: {
    statusFilter(status) {
      const statusMap = {
        1: 'success',
        2: 'danger'
      }
      return statusMap[status]
    },
    statusText(value) {
      return value === 1 ? '在线' : '报警'; // 1 显示为 '在线'，2 显示为 '报警'
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
        id: '',
        name: '',
        type: '',
        state: '',
        startDate: '',
        endDate: '',
        sort: '+id'
      },
      statusOptions: [{ label: '在线', key: '1' }, { label: '报警', key: '2' }],
      typeOptions,
      sortOptions: [{ label: 'ID升序', key: '+id' }, { label: 'ID降序', key: '-id' }],
      temp: {
        name: '',
        type: '',
        description: '',
        username: ''
      },
      customLabelWidth: '80px',
      dialogFormVisible: false,
      showDataStatistic: false,
      dialogStatus: '',
      textMap: {
        update: '修改设备信息',
        create: '创建设备'
      },
      rules: {
        name: [{ required: true, message: '请填写设备名', trigger: 'blur' }],
        type: [{ required: true, message: '请选择设备类型', trigger: 'change' }]
      },
      isMobile: false,
      valueData: {
        data: []
      },
    }
  },
  computed: {
    ...mapGetters([
      'deviceList',
      'deviceNum',
      'username'
    ])
  },
  created() {
    this.listQuery.username = this.username
    this.getList()
    // 获取屏幕宽度
    const screenWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    // 设置isMobile值，根据屏幕宽度调整
    this.isMobile = screenWidth <= 550 ? true : false;
  },
  methods: {
    getList() {
      this.listLoading = true
      searchDevice(this.listQuery).then((response) => {
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
    sortChange(data) {
      const { prop, order } = data
      if (prop === 'id') {
        this.sortByID(order)
      }
    },
    sortByID(order) {
      if (order === 'ascending') {
        this.listQuery.sort = '+id'
      } else {
        this.listQuery.sort = '-id'
      }
      this.handleFilter()
    },
    resetTemp() {
      this.temp = {
        name: '',
        type: '',
        description: '',
        username: this.username,
        device_id: ''
      }
    },
    handleCreate() {
      this.resetTemp()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
      this.showDataStatistic = false
    },
    createData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          createDevice(this.temp).then((response) => {
            this.list.unshift(this.temp)
            this.dialogFormVisible = false
            this.$notify({
              title: response.code == 20000 ? '成功' : '失败',
              message: response.msg,
              type: response.code == 20000 ? 'success' : 'fail',
              duration: 2000
            })
            this.refreshPage()
          })
        }
      })
    },
    handleUpdate(row) {
      this.temp = Object.assign({}, row) // copy obj
      this.temp.device_id = row.device_id
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
      getDataList(row.device_id).then((response) => {
        this.valueData.data = response.data.dataList
      })
      this.showDataStatistic = true
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          const tempData = Object.assign({}, this.temp)
          modifyDevice(tempData).then((response) => {
            const index = this.list.findIndex(v => v.id === this.temp.id)
            this.list.splice(index, 1, this.temp)
            this.dialogFormVisible = false
            this.$notify({
              title: response.code == 20000 ? '成功' : '失败',
              message: response.msg,
              type: response.code == 20000 ? 'success' : 'fail',
              duration: 2000
            })
            this.refreshPage()
          })
        }
      })
    },
    handleDelete(row, index) {
      deleteDevice(row.device_id).then((response) => {
        this.$notify({
          title: response.code == 20000 ? '成功' : '失败',
          message: response.msg,
          type: response.code == 20000 ? 'success' : 'fail',
          duration: 2000
        })
        this.list.splice(index, 1)
      })
    },
    getSortClass: function(key) {
      const sort = this.listQuery.sort
      return sort === `+${key}` ? 'ascending' : 'descending'
    },
    refreshPage() {
      window.location.reload();
    }
  }
}
</script>

<style lang="scss" scoped>
.search-device {
  margin-left: 10px;
  width: 120px;
  @media (max-width: 550px) {
    margin-left: 0px;
  }
}

.create-device {
  margin-left: 10px;
  width: 120px;
  @media (max-width: 550px) {
    margin-left: 0px;
    margin-top: 5px;
  }
}

.sort {
  width: 140px;
  @media (max-width: 550px) {
    display: block;
  }
}

.description {
  width: 500px;
  @media (max-width: 550px) {
    width: 250px;
  }
}

.filter-item {
  @media (max-width: 550px) {
    width: 160px !important;
  }
}

.table {
  width: 350px;
  margin-left: 20px;
  @media (max-width: 550px) {
    width: 240px;
    margin-left: 0px;
  }
}

.statistic {
  background:#fff;
  padding: 10px 0px 0;
  margin-bottom: 0px;
  width: 800px;
  @media (max-width: 550px) {
    margin-bottom: 0px;
  }
}

</style>
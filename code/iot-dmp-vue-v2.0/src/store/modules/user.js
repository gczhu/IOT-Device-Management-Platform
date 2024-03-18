import { login, logout, getInfo, register } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { resetRouter } from '@/router'
import CryptoJS from 'crypto-js'

const getDefaultState = () => {
  return {
    token: getToken(),
    username: '',
    email: '',
    deviceNum: '',
    onlineNum: '',
    alertNum: '',
    MQTTMsgNum: '',
    dateList: '',
    hourList: '',
    deviceNumList: '',
    onlineNumList: '',
    alertNumList: '',
    MQTTMsgNumList: '',
    sensorNum: '',
    actuatorNum: '',
    embeddedNum: '',
    communicationNum: '',
    controllerNum: '',
    MQTTMsgList: '',
    deviceList: ''
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_USERNAME: (state, username) => {
    state.username = username
  },
  SET_EMAIL: (state, email) => {
    state.email = email
  },
  SET_DEVICENUM: (state, deviceNum) => {
    state.deviceNum = deviceNum
  },
  SET_ONLINENUM: (state, onlineNum) => {
    state.onlineNum = onlineNum
  },
  SET_ALERTNUM: (state, alertNum) => {
    state.alertNum = alertNum
  },
  SET_MQTTMSGNUM: (state, MQTTMsgNum) => {
    state.MQTTMsgNum = MQTTMsgNum
  },
  SET_DATELIST: (state, dateList) => {
    state.dateList = dateList
  },
  SET_HOURLIST: (state, hourList) => {
    state.hourList = hourList
  },
  SET_DEVICENUMList: (state, deviceNumList) => {
    state.deviceNumList = deviceNumList
  },
  SET_ONLINENUMList: (state, onlineNumList) => {
    state.onlineNumList = onlineNumList
  },
  SET_ALERTNumList: (state, alertNumList) => {
    state.alertNumList = alertNumList
  },
  SET_MQTTMSGNUMLIST: (state, MQTTMsgNumList) => {
    state.MQTTMsgNumList = MQTTMsgNumList
  },
  SET_SENSORNUM: (state, sensorNum) => {
    state.sensorNum = sensorNum
  },
  SET_ACTUATORNUM: (state, actuatorNum) => {
    state.actuatorNum = actuatorNum
  },
  SET_EMBEDDEDNUM: (state, embeddedNum) => {
    state.embeddedNum = embeddedNum
  },
  SET_COMMUNICATIONNUM: (state, communicationNum) => {
    state.communicationNum = communicationNum
  },
  SET_CONTROLLERNUM: (state, controllerNum) => {
    state.controllerNum = controllerNum
  },
  SET_MQTTMSGLIST: (state, MQTTMsgList) => {
    state.MQTTMsgList = MQTTMsgList
  },
  SET_DEVICELIST: (state, deviceList) => {
    state.deviceList = deviceList
  }
}

const actions = {
  // user login
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    const secretKey = '3210105954'

    // 加密
    // 将密码和密钥转换为字节数组
    const passwordBytes = CryptoJS.enc.Utf8.parse(password);
    const secretKeyBytes = CryptoJS.enc.Utf8.parse(secretKey);

    // 使用 AES 加密算法进行加密
    const encrypted = CryptoJS.AES.encrypt(passwordBytes, secretKeyBytes, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    });

    return new Promise((resolve, reject) => {
      login({ username: username, password: encrypted.toString().substring(0, 10)}).then(response => {
        const { data } = response
        commit('SET_TOKEN', data.token)
        setToken(data.token)
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getInfo(state.token).then(response => {
        const { data } = response

        if (!data) {
          return reject('Verification failed, please Login again.')
        }

        const { username, email, deviceNum, onlineNum, alertNum, MQTTMsgNum, dateList, hourList, deviceNumList, onlineNumList, alertNumList, MQTTMsgNumList, sensorNum, actuatorNum, embeddedNum, communicationNum, controllerNum,  MQTTMsgList, deviceList} = data

        commit('SET_USERNAME', username)
        commit('SET_EMAIL', email)
        commit('SET_DEVICENUM', deviceNum)
        commit('SET_ONLINENUM', onlineNum)
        commit('SET_ALERTNUM', alertNum)
        commit('SET_MQTTMSGNUM', MQTTMsgNum)
        commit('SET_DATELIST', dateList)
        commit('SET_HOURLIST', hourList)
        commit('SET_DEVICENUMList', deviceNumList)
        commit('SET_ONLINENUMList', onlineNumList)
        commit('SET_ALERTNumList', alertNumList)
        commit('SET_MQTTMSGNUMLIST', MQTTMsgNumList)
        commit('SET_SENSORNUM', sensorNum)
        commit('SET_ACTUATORNUM', actuatorNum)
        commit('SET_EMBEDDEDNUM', embeddedNum)
        commit('SET_COMMUNICATIONNUM', communicationNum)
        commit('SET_CONTROLLERNUM', controllerNum)
        commit('SET_MQTTMSGLIST', MQTTMsgList)
        commit('SET_DEVICELIST', deviceList)
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout(state.token).then(() => {
        removeToken() // must remove  token  first
        resetRouter()
        commit('RESET_STATE')
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user register
  register({ commit }, userInfo) {
    const { username, password, password1, email } = userInfo
    const secretKey = '3210105954'

    // 加密
    // 将密码和密钥转换为字节数组
    const passwordBytes = CryptoJS.enc.Utf8.parse(password);
    const secretKeyBytes = CryptoJS.enc.Utf8.parse(secretKey);

    // 使用 AES 加密算法进行加密
    const encrypted = CryptoJS.AES.encrypt(passwordBytes, secretKeyBytes, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    });

    return new Promise((resolve, reject) => {
      register({ username: username, email: email, password: encrypted.toString().substring(0, 10)}).then(response => {
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
      commit('RESET_STATE')
      resolve()
    })
  }

}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}


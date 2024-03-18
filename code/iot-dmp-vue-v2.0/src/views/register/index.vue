<template>
  <div class="register-container">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form" auto-complete="off" label-position="left">

      <div class="title-container">
        <img src="../../assets/logo.png" class="logo">
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="username"
          v-model="registerForm.username"
          placeholder="用户名"
          name="username"
          type="text"
          tabindex="1"
          auto-complete="off"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password"
          v-model="registerForm.password"
          :type="passwordType"
          placeholder="密码"
          name="password"
          tabindex="2"
          auto-complete="off"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="password1">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password1"
          v-model="registerForm.password1"
          :type="passwordType"
          placeholder="确认密码"
          name="password1"
          tabindex="3"
          auto-complete="off"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="email">
        <span class="svg-container">
          <svg-icon icon-class="email" />
        </span>
        <el-input
          ref="email"
          v-model="registerForm.email"
          placeholder="邮箱地址"
          name="email"
          type="text"
          tabindex="4"
          auto-complete="off"
        />
      </el-form-item>

      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:10px;" @click.native.prevent="handleRegister">注册</el-button>

      <div class="tips">
        <p>已经拥有账号？<router-link to="/login" class="ad-register-text">返回登录页面</router-link></p>
      </div>

    </el-form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (value.length == 0) {
        callback(new Error('用户名不能为空'));
      } else if (value.length < 6 || value.length > 10) {
        callback(new Error('用户名长度应在6-10个字符'));
      } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        callback(new Error('用户名只能包含英文字母、数字和下划线'));
      } else {
        axios.get( process.env.VUE_APP_BASE_API + '/user/checkUsername', { params: { username: value } })
        .then(response => {
            if (response.data.flag == false) {
                callback(new Error('用户名重复'))
            } else {
                callback()
            }
        })
        .catch(error => {
            console.error(error);
        });
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length == 0) {
        callback(new Error('密码不能为空'));
      } else if (value.length < 6 || value.length > 10) {
        callback(new Error('密码长度应在6-10个字符'));
      } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        callback(new Error('密码只能包含英文字母、数字和下划线'));
      } else {
        callback();
      }
    }
    const validateEmail = (rule, value, callback) => {
      if (value.length == 0) {
        callback(new Error('邮箱地址不能为空'));
      } else if (!/^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/.test(value)) {
        callback(new Error('邮箱地址格式不正确'));
      } else {
        axios.get( process.env.VUE_APP_BASE_API + '/user/checkEmail', { params: { email: value } })
        .then(response => {
            if (response.data.flag == false) {
                callback(new Error('邮箱地址重复'))
            } else {
                callback()
            }
        })
        .catch(error => {
            console.error(error);
        });
      }
    }
    const validatePasswordMatch = (rule, value, callback) => {
      if (value != '' && value != this.registerForm.password) {
        callback(new Error('密码不一致'));
      } else {
        callback();
      }
    }
    return {
      registerForm: {
        username: '',
        password: '',
        password1: '',
        email: ''
      },
      registerRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        password1: [{ validator: validatePasswordMatch }],
        email: [{ required: true, trigger: 'blur', validator: validateEmail }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined,
      secretKey: '3210105954'
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    },
    'registerForm.password': function(newValue) {
      this.$refs.registerForm.validateField('password1');
    },
    'registeFrom.password1': function(newValue) {
      this.$refs.registerForm.validateField('password1');
    }
  },
  methods: {
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('user/register', this.registerForm).then(() => {
            this.$router.push({ path: this.redirect || '/login' })
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss">
/* 修复input 背景不协调 和光标变色 */
/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

$bg:#283443;
$light_gray:#fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .register-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.register-container {

  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $cursor !important;
        background-color: transparent !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }

  .ad-register-text {
    color: #00BFFF;
    transition: color 0.3s;
  }

  .ad-register-text:hover {
    color: #1E90FF;
  }
}
</style>

<style lang="scss" scoped>
$dark_gray:#889aa4;
$light_gray:#eee;

.register-container {
  min-height: 100%;
  width: 100%;
  background-image: url('../../assets/background.png');
  background-size: cover;
  background-position: center;
  overflow: hidden;

  .register-form {
    position: absolute;
    top: 45%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 520px;
    max-width: 100%;
    padding: 160px 35px 0;
    margin: 0 auto;
    overflow: hidden;
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;
    
    .logo {
      width: 40vh;
      margin: -150px auto 40px 26px;
      display: block;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }
}
</style>

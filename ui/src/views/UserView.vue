<script setup>
import {ref} from "vue";
import api from "../libs/api.js";
import Util from "../libs/Util.js";
const validationType = ref('email');
const noValidate = ref(!localStorage.getItem('token'))
const timer = ref(null)
const time = ref(0)
const sending = ref(false)
const form = ref({
  email: '',
  mobile: '',
  code: ''
})
const sendCode = () => {
  if (sending.value) return
  if (time.value > 0) return
  if (validationType.value === 'email' && !form.value.email) {
    Util.alert('Error', 'Please enter your email.')
    return;
  }
  if (validationType.value === 'mobile' && !form.value.mobile) {
    Util.alert('Error', 'Please enter your mobile.')
    return;
  }
  sending.value = true
  api.post('/api/v1/user/sendVerificationCode', {
    ... form.value, type: validationType.value
  }).then(res => {
    time.value = 60
    timer.value = setInterval(() => {
      time.value--
      if (time.value === 0) {
        clearInterval(timer.value)
      }
    }, 1000)
  }).finally(() => {
    sending.value = false
  })
}
const submit = () => {
  if (validationType.value === 'email' && !form.value.email) {
    Util.alert('Error', 'Please enter your email.')
    return;
  }
  if (validationType.value === 'mobile' && !form.value.mobile) {
    Util.alert('Error', 'Please enter your mobile.')
    return;
  }
  if (!form.value.code) {
    Util.alert('Error', 'Please enter the verification code.')
    return;
  }
  api.post('/api/v1/user/verify', {
    ... form.value, type: validationType.value
  }).then(res => {
    localStorage.setItem('token', res.data.token)
    user.value = res.data.user
    noValidate.value = false
  }).catch(err => {
    Util.alert('Error', err.message)
  })
}
const user = ref(null)
if (!noValidate.value) {
  api.get('/api/v1/user').then(res => {
    user.value = res.data
  }).catch(err => {
    noValidate.value = true
  })
}
</script>

<template>
<div class="user-view" :class="{vali: noValidate}">
  <div class="validation-box" :class="{show: noValidate}">
    <!-- 验证你是人类 -->
    <div class="title">Verify that you are human</div>
    <div class="tabs">
      <div class="tab" :class="{active: validationType === 'email'}" @click="validationType = 'email'">Email</div>
      <div class="tab" :class="{active: validationType === 'mobile'}" @click="validationType = 'mobile'">Mobile</div>
    </div>
    <div class="tab-content">
      <div class="tab-pane" :class="{active: validationType === 'email'}">
        <div class="input-box">
          <input type="text" placeholder="Email" v-model="form.email"/>
        </div>
        <div class="input-box validation">
          <input type="text" placeholder="Verification code" v-model="form.code" />
          <button @click="sendCode">
            {{sending ? 'Sending...' : 'Send code' + (time > 0 ? ' (' + time + 's)' : '')}}
          </button>
        </div>
      </div>
      <div class="tab-pane" :class="{active: validationType === 'mobile'}">
        <div class="input-box">
          <input type="text" placeholder="Mobile" v-model="form.mobile" />
        </div>
        <div class="input-box validation">
          <input type="text" placeholder="Verification code" v-model="form.code" />
          <button @click="sendCode">
            {{sending ? 'Sending...' : 'Send code' + (time > 0 ? ' (' + time + 's)' : '')}}
          </button>
        </div>
      </div>
      <button class="submit" @click="submit">Submit</button>
    </div>
  </div>
  <div class="valied" :class="{show: !noValidate}" v-if="!noValidate">
    <div class="user-info">
      <div class="title">
        <img src="/icons/finish.svg" />
        Verification successful!
      </div>
      <div class="content">{{user.nicker}}</div>
    </div>
  </div>
</div>
</template>

<style scoped lang="less">
.user-view{
  // 上下居中
  &.vali{
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
  }

  &.valied{
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
  }


  .validation-box{
    display: none;
    width: 400px;
    margin: 0 auto;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 5px;
    background-color: #fff;
    &.show{
      display: block;
    }

    .title{
      font-size: 20px;
      font-weight: bold;
      margin-bottom: 20px;
    }
    .tabs{
      display: flex;
      .tab{
        flex: 1;
        text-align: center;
        padding: 10px 0;
        cursor: pointer;
        &:hover{
          background-color: #f0f0f0;
        }
        &.active{
          border-bottom: #8c56fc 2px solid;
        }
      }
    }
    .tab-content{
      margin-top: 10px;
      .tab-pane{
        display: none;
        &.active{
          display: block;
        }
        .input-box{
          margin-bottom: 10px;
          input{
            width: calc(100% - 20px);
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            outline: none;
          }

          &.validation {
            display: flex;
            input {
              flex: 1;
            }
            button {
              padding: 10px;
              border: 1px solid #8c56fc;
              border-radius: 5px;
              background-color: #8c56fc;
              color: #fff;
              cursor: pointer;
              margin-left: 10px;
              width: 120px;

              &:hover {
                background-color: #7a4cf2;
              }
            }
          }
        }
      }
      .submit{
        width: 100%;
        padding: 10px;
        border: 1px solid #8c56fc;
        border-radius: 5px;
        background-color: #8c56fc;
        color: #fff;
        cursor: pointer;
        margin-top: 10px;

        &:hover {
          background-color: #7a4cf2;
        }
      }
    }
  }
  .valied{
    display: none;
    justify-content: center;
    align-items: center;
    height: 100vh;
    &.show{
      display: flex;
    }
    .user-info{
      width: 400px;
      margin: 0 auto;
      padding: 20px;
      border: 1px solid #ccc;
      border-radius: 5px;
      background-color: #fff;

      .title{
        font-size: 20px;
        font-weight: bold;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        img{
          width: 30px;
          height: 30px;
          margin-right: 10px;
        }
      }
      .content{
        font-size: 14px;
      }
    }
  }

}
</style>
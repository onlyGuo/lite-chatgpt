<script setup>
import router from "./router/index.js";

const goto = (path) => {
  if (router.currentRoute.value.path === path) {
    return
  }
  router.push({
    path: path
  })
}
</script>

<template>
  <div class="left-nav">
    <div class="left">
      <div class="header">
        <img src="./assets/vue.svg" alt="logo" />
        <div class="title">Lite-GPT</div>
      </div>
      <div class="nav-list">
        <div class="item" :class="{active: router.currentRoute.value.path === '/chat' || router.currentRoute.value.path === '/'}" @click="goto('/chat')">
          <img src="./assets/chat.svg" alt="chat" />
        </div>
        <div class="item" :class="{active: router.currentRoute.value.path === '/user'}" @click="goto('/user')">
          <img src="./assets/user.svg" alt="user" />
        </div>
        <div class="item" :class="{active: router.currentRoute.value.path === '/bbs'}" @click="goto('/bbs')">
          <img src="./assets/bbs.svg" alt="bbs" />
        </div>
      </div>
      <div class="buttom">
        <div class="item" :class="{active: router.currentRoute.value.path === '/setting'}" @click="goto('/setting')">
          <img src="./assets/setting.svg" alt="setting" />
        </div>
      </div>
    </div>
    <div class="right">
      <router-view/>
    </div>
  </div>
</template>

<style scoped lang="less">

.left-nav{
  display: flex;
  height: 100vh;
  .left{
    width: 70px;
    background-color: #a578ff;
    color: #fff;
    .header{
      text-align: center;
      padding-top: 20px;
      padding-bottom: 20px;
      img{
        width: 30px;
        height: 30px;
      }
      .title{
        font-size: 12px;
        text-align: center;
      }
    }
    .nav-list{
      height: calc(100vh - 160px);
      margin-top: 10px;
      .item{
        padding: 10px 0;
        text-align: center;
        transition: background-color 0.3s;
        img{
          width: 30px;
          height: 30px;
        }
        &:hover{
          background-color: #8c56fc;
        }
        &.active{
          background-color: #8c56fc;
          box-shadow: 0 3px 3px rgba(115, 84, 178, .3);
        }
      }
    }
    .buttom{
      .item{
        padding: 10px 0;
        text-align: center;
        transition: background-color 0.3s;
        img{
          width: 30px;
          height: 30px;
        }
        &:hover{
          background-color: #8c56fc;
        }
        &.active{
          background-color: #8c56fc;
        }
      }
    }
  }
  .right{
    flex: 1;
  }
}
</style>

<script setup>
import router from "./router/index.js";
import Util from "./libs/Util.js";

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
  <div>
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
    <div class="alerts" :class="{show: Util.alertMessages.value.length > 0}">
      <div v-for="a in Util.alertMessages.value" :key="a.id">
        <div class="alert" :class="a.type">
          <div class="title">
            <div class="text">{{a.title}}</div>
            <div class="close" @click="Util.closeAlert(a.id)">
              <img src="./assets/close.svg" alt="close" />
            </div>
          </div>
          <div class="message">{{a.content}}</div>
          <div class="btns">
            <div class="default-btn" @click="Util.closeAlert(a.id)">OK</div>
          </div>
        </div>
      </div>
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
.alerts{
  display: none;
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  //background-color: rgba(255, 255, 255, .5);
  //backdrop-filter: blur(5px);
  justify-content: center;
  align-items: center;
  transition: opacity .3s;
  &.show{
    display: flex;
  }

  .alert{
    min-width: 300px;
    max-width: 800px;
    margin-bottom: 10px;
    padding: 10px;
    border-radius: 5px;
    background-color: rgba(255, 255, 255, .5);
    backdrop-filter: blur(10px);
    box-shadow: 0 3px 10px rgba(0, 0, 0, .1);
    border: 1px solid #ccc;
    .title{
      display: flex;
      justify-content: space-between;
      .text{
        font-size: 16px;
        font-weight: bold;
      }
      .close{
        cursor: pointer;
        img{
          width: 20px;
          height: 20px;
        }
      }
    }
    .message{
      margin-top: 10px;
      // 换行
      white-space: pre-wrap;
      // 允许断行
      word-break: break-all;
    }
    .btns{
      margin-top: 10px;
      // 右对齐
      text-align: right;
      .default-btn{
        padding: 5px 10px;
        border: 1px solid #8c56fc;
        border-radius: 5px;
        cursor: pointer;
        display: inline-block;
        background-color: #8c56fc;
        color: white;
        transition: all .3s;
        &:hover{
          color: #8c56fc;
          background-color: transparent;
        }
      }
    }
  }
}
</style>

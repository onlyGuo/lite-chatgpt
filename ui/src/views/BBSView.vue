<script setup>

import ScrollBar from "../components/ScrollBar.vue";
</script>

<template>
<div class="bbs">
  <div class="header">
    <div class="title">Bulletin Board System</div>
    <div class="search">
      <input type="text" placeholder="Search" />
      <img src="../assets/search.svg" />
    </div>
  </div>
  <div class="center-content">
    <div class="left-classify-list">
      <div class="classify active">最新</div>
      <div class="classify">热门</div>
      <div class="classify">求助</div>
      <div class="classify">闲聊</div>
    </div>
    <div class="right-posts">
      <scroll-bar>
        <div style="height: 80px"></div>
        <div class="post" v-for="i in 20" :key="i">
          <div class="left">
            <div class="avatar"></div>
          </div>
          <div class="center">
            <div class="top">
              <div class="nicker">张三</div>
              <div class="level">Lv1</div>
              <div class="create-time">1分钟前</div>
            </div>
            <div class="content">
              <div class="text">
                影评类网站（豆瓣，IMDB），资讯网站（hacker news),问题回答网站（知乎，stackoverflow），视频网站（腾讯视频，爱奇艺，优酷）都会使用热榜，吸引用户来观看优质的内容。

                可以根据几个重大特征分为几种不同的榜单

                作者：Howyi
                链接：https://www.jianshu.com/p/88a2999c807d
                来源：简书
                著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
              </div>
              <div class="images">
                <div class="image">
                  <img src="../assets/vue.svg" />
                </div>
                <div class="image">
                  <img src="../assets/vue.svg" />
                </div>
                <div class="image">
                  <img src="../assets/vue.svg" />
                </div>
              </div>
              <div class="bottom-tools">
                <div class="btn">Share(14)</div>
                <div class="btn">Comment(21)</div>
                <div class="btn">Like(36)</div>
              </div>
            </div>
          </div>
          <div class="right">

          </div>
        </div>
      </scroll-bar>
    </div>
  </div>
</div>
</template>

<style scoped lang="less">
.bbs{
  background-color: #f0f2f5;
  .header{
    background-color: rgba(255, 255, 255, .5);
    padding: 10px;
    box-shadow: 1px 1px 7px -4px rgba(0,0,0,0.2);
    display: flex;
    justify-content: space-between;
    position: fixed;
    left: 70px;
    right: 0;
    z-index: 10;
    backdrop-filter: blur(20px);
    .title{
      font-size: 20px;
      font-weight: bold;
      line-height: 37px;
      height: 37px;
    }
    .search{
      // 上下居中
      display: flex;
      align-items: center;
      input{
        width: 70px;
        padding: 5px 30px 5px 10px;
        border-radius: 5px;
        border: 1px solid transparent;
        outline: none;
        transition: all .3s;
        &:focus {
          border-color: #a578ff;
          width: 300px;
        }
      }
      img{
        width: 20px;
        height: 20px;
        margin-left: -25px;
      }
    }
  }

  .center-content{
    max-width: 1000px;
    margin: 0 auto;
    height: 100vh;
    display: flex;

    .left-classify-list{
      width: 200px;
      background-color: white;
      height: max-content;
      border-radius: 10px;
      overflow: hidden;
      box-shadow: 1px 1px 7px -4px rgba(0,0,0,0.1);
      margin-top: 80px;
      padding-top: 10px;
      padding-bottom: 10px;
      .classify{
        padding: 10px;
        text-align: center;
        cursor: pointer;
        transition: all .3s;
        &:hover{
          background-color: #fcfcfc;
          color: #a578ff;
        }
        &.active{
          background-color: #ece3fd;
          color: #a578ff;
          border-right: #8c56fc 3px solid;
        }
      }

    }
    .right-posts{
      margin-left: 20px;
      flex: 1;
      height: 100vh;
      .post{
        display: flex;
        margin-bottom: 10px;
        background-color: white;
        padding: 10px;
        border-radius: 10px;
        overflow: hidden;
        box-shadow: 1px 1px 7px -4px rgba(0,0,0,0.1);
        .left{
          .avatar{
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background-color: #e3e3e3;
          }
        }
        .center{
          flex: 1;
          padding: 10px;
          .top{
            display: flex;
            // 上下居中
            align-items: center;
            .nicker{
              margin-right: 10px;
              color: #8c56fc;
              font-size: 18px;
            }
            .level{
              margin-right: 10px;
              background-color: #8c56fc;
              color: white;
              font-size: 12px;
              padding: 2px 5px;
              height: 14px;
              border-radius: 3px;
            }
            .create-time{
              margin-right: 10px;
              font-size: 12px;
            }
          }
          .content{
            .text{
              margin-bottom: 10px;
            }
            .images{
              display: flex;
              .image{
                width: 100px;
                height: 100px;
                margin-right: 10px;
                img{
                  width: 100%;
                  height: 100%;
                }
              }
            }
            .bottom-tools{
              display: flex;
              .btn{
                padding: 5px 10px;
                margin-right: 10px;
                cursor: pointer;
                border-radius: 4px;
                transition: all .3s;
                &:hover{
                  background-color: #f0f0f0;
                }
              }
            }
          }
        }
        .right{
          background-color: #f0f0f0;
        }
      }
    }
  }
}
</style>
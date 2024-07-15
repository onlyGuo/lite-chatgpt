<script>
export default {
  name: 'ScrollBar',
  data(){
    return {
      showY: false,
      showX: false
    }
  },
  mounted(){
    this.init()
  },
  methods: {
    init() {
      this.scrollBoxBarYThumb = this.$refs.scrollBoxBarYThumb
      this.scrollBoxBarY = this.$refs.scrollBoxBarY
      this.scrollBoxBarXThumb = this.$refs.scrollBoxBarXThumb
      this.scrollBoxBarX = this.$refs.scrollBoxBarX
      this.scrollBoxContent = this.$refs.scrollBoxContent
      this.scrollBoxContent.addEventListener('scroll', this.scroll)
      this.scrollBoxBarYThumb.addEventListener('mousedown', this.mouseDownY)
      this.scrollBoxBarXThumb.addEventListener('mousedown', this.mouseDownX)
      this.scrollBoxBarY.addEventListener('mousedown', this.mouseDownY)
      this.scrollBoxBarX.addEventListener('mousedown', this.mouseDownX)
      this.scrollBoxBarYThumb.style.height = this.scrollBoxContent.clientHeight / this.scrollBoxContent.scrollHeight * this.scrollBoxBarY.clientHeight + 'px'
      this.scrollBoxBarXThumb.style.width = this.scrollBoxContent.clientWidth / this.scrollBoxContent.scrollWidth * this.scrollBoxBarX.clientWidth + 'px'
      this.showY = this.scrollBoxContent.clientHeight < this.scrollBoxContent.scrollHeight
      this.showX = this.scrollBoxContent.clientWidth < this.scrollBoxContent.scrollWidth
    },
    scroll() {
      this.scrollBoxBarYThumb.style.top = this.scrollBoxContent.scrollTop / this.scrollBoxContent.scrollHeight * this.scrollBoxBarY.clientHeight + 'px'
      this.scrollBoxBarXThumb.style.left = this.scrollBoxContent.scrollLeft / this.scrollBoxContent.scrollWidth * this.scrollBoxBarX.clientWidth + 'px'
    },
    mouseDownY(e) {
      e.preventDefault()
      this.startY = e.clientY
      this.startScrollTop = this.scrollBoxContent.scrollTop
      document.addEventListener('mousemove', this.mouseMoveY)
      document.addEventListener('mouseup', this.mouseUpY)
    },
    mouseMoveY(e) {
      e.preventDefault()
      let moveY = e.clientY - this.startY
      this.scrollBoxContent.scrollTop = this.startScrollTop + moveY / this.scrollBoxBarY.clientHeight * this.scrollBoxContent.scrollHeight
    },
    mouseUpY() {
      document.removeEventListener('mousemove', this.mouseMoveY)
      document.removeEventListener('mouseup', this.mouseUpY)
    },
    mouseDownX(e) {
      e.preventDefault()
      this.startX = e.clientX
      this.startScrollLeft = this.scrollBoxContent.scrollLeft
      document.addEventListener('mousemove', this.mouseMoveX)
      document.addEventListener('mouseup', this.mouseUpX)
    },
    mouseMoveX(e) {
      e.preventDefault()
      let moveX = e.clientX - this.startX
      this.scrollBoxContent.scrollLeft = this.startScrollLeft + moveX / this.scrollBoxBarX.clientWidth * this.scrollBoxContent.scrollWidth
    },
    mouseUpX() {
      document.removeEventListener('mousemove', this.mouseMoveX)
      document.removeEventListener('mouseup', this.mouseUpX)
    },
    toBottom() {
      this.scrollBoxContent.scrollTop = this.scrollBoxContent.scrollHeight
    }
  },
  // 监听内容变化
  updated() {
    this.init()
  }
}
</script>

<template>
  <div class="scroll-box">
    <div class="scroll-box-content" ref="scrollBoxContent">
      <slot></slot>
    </div>
    <div class="scroll-box-bar-y" ref="scrollBoxBarY">
      <div class="scroll-box-bar-y-thumb" ref="scrollBoxBarYThumb" :class="{showY: showY}"></div>
    </div>
    <div class="scroll-box-bar-x" ref="scrollBoxBarX">
      <div class="scroll-box-bar-x-thumb" ref="scrollBoxBarXThumb" :class="{showX: showX}"></div>
    </div>
  </div>
</template>

<style scoped lang="less">
.scroll-box {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;

  .scroll-box-content {
    position: absolute;
    width: 100%;
    height: 100%;
    overflow: auto;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  &:hover {
    .scroll-box-bar-y {
      .scroll-box-bar-y-thumb.showY {
        background-color: #cfcfcf;
      }
    }

    .scroll-box-bar-x {
      .scroll-box-bar-x-thumb.showX {
        background-color: #cfcfcf;
      }
    }
  }

  .scroll-box-bar-y {
    position: absolute;
    right: 0;
    top: 0;
    width: 6px;
    height: 100%;
    background-color: transparent;

    .scroll-box-bar-y-thumb {
      position: absolute;
      width: 100%;
      height: 50px;
      background-color: transparent;
      border-radius: 3px;
      transition: height, background-color .3s;

      &.showY:hover {
        background-color: #afafaf;
      }
    }
  }

  .scroll-box-bar-x {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 100%;
    height: 6px;
    background-color: transparent;

    .scroll-box-bar-x-thumb {
      position: absolute;
      height: 100%;
      width: 50px;
      background-color: transparent;
      border-radius: 3px;

      &.showX:hover {
        background-color: #afafaf;
      }
    }
  }
}
</style>

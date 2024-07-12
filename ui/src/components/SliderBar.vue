<script setup>
import {watch} from "vue";

const props = defineProps({
  value: Number,
  min: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 100
  },
  step: {
    type: Number,
    default: 1
  },
  disabled: {
    type: Boolean,
    default: false
  },
})

const emit = defineEmits(['update:value'])

const updateValue = (value) => {
  emit('update:value', parseInt(value))
}
</script>

<template>
  <div class="slider-bar">
    <input
      type="range"
      :value="value"
      :min="min"
      :max="max"
      :step="step"
      :disabled="disabled"
      @input="updateValue($event.target.value)"
    />
  </div>
</template>

<style scoped lang="less">
.slider-bar{
  input[type=range] {
    width: 100%;
    -webkit-appearance: none;
    background: transparent;
  }
  input[type=range]::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 15px;
    height: 15px;
    background: #fff;
    border-radius: 50%;
    cursor: pointer;
    box-shadow: 0 0 2px 0 rgba(10, 10, 10, 0.2);
    margin-top: -5px;
  }
  input[type=range]::-webkit-slider-runnable-track {
    width: 100%;
    height: 5px;
    background: #dddddd;
    border-radius: 5px;
  }
  input[type=range]:focus {
    outline: none;
  }
  input[type=range]:focus::-webkit-slider-runnable-track {
    background: #dddddd;
  }
  input[type=range]::-moz-range-thumb {
    width: 15px;
    height: 15px;
    background: #fff;
    border-radius: 50%;
    cursor: pointer;
    box-shadow: 0 0 2px 0 rgba(10, 10, 10, 0.2);
    margin-top: -5px;
  }
  input[type=range]::-moz-range-track {
    width: 100%;
    height: 5px;
    background: #dddddd;
    border-radius: 5px;
  }
  input[type=range]::-ms-thumb {
    width: 20px;
    height: 20px;
    background: #fff;
    border-radius: 50%;
    cursor: pointer;
  }
  input[type=range]::-ms-track {
    width: 100%;
    height: 5px;
    background: #dddddd;
    border-radius: 5px;
  }
}
</style>
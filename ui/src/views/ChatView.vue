<script setup>

import ChatBox from "../components/ChatBox.vue";
import {ref} from "vue";
import axios from "axios";
import Util from "../libs/Util.js";
import api from "../libs/api.js";

const defaultList = ref([])
const emptyList = [
  {
    id: '-1',
    name: 'ChatGPT 3.5',
    lastContent: 'Not message yet.',
    avatar: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNzE5OTgwNTczODkzIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE0MDU1IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxwYXRoIGQ9Ik04MTUuNjE2IDBIMjA4LjM4NEM5My4zIDAgMCA5NC4wNjggMCAyMTAuMDgzdjYwMy44MzRDMCA5MjkuOTU1IDkzLjMgMTAyNCAyMDguMzg0IDEwMjRoNjA3LjIzMkM5MzAuNyAxMDI0IDEwMjQgOTI5Ljk1NSAxMDI0IDgxMy45MTdWMjEwLjA4M0MxMDI0IDk0LjA2OCA5MzAuNyAwIDgxNS42MTYgMHoiIGZpbGw9IiMxMEEzN0YiIHAtaWQ9IjE0MDU2Ij48L3BhdGg+PHBhdGggZD0iTTc1Ny4zNjQgNDYwLjAzMkExNDIuODI1IDE0Mi44MjUgMCAwIDAgNzQ1LjEgMzQyLjgwN2ExNDQuNDA3IDE0NC40MDcgMCAwIDAtMTU1LjQ2Mi02OS4yNiAxNDIuNzA4IDE0Mi43MDggMCAwIDAtMTA2LjcyOS00Ny45ODhoLTEuMjU3YTE0NC4zODQgMTQ0LjM4NCAwIDAgMC0xMzcuMzU1IDk5LjkzMyAxNDIuNzU1IDE0Mi43NTUgMCAwIDAtOTUuNDE4IDY5LjIzNyAxNDQuNDMgMTQ0LjQzIDAgMCAwIDE3Ljc1NyAxNjkuMjYyIDE0Mi44MjUgMTQyLjgyNSAwIDAgMCAxMi4yNDEgMTE3LjIwMiAxNDQuMzg0IDE0NC4zODQgMCAwIDAgMTU1LjQ2MiA2OS4yNkExNDIuNzU1IDE0Mi43NTUgMCAwIDAgNTQxLjA5IDc5OC40NGgxLjI4YTE0NC4zMzcgMTQ0LjMzNyAwIDAgMCAxMzcuMzU2LTEwMC4wMDMgMTQyLjczMiAxNDIuNzMyIDAgMCAwIDk1LjQxOC02OS4yMzcgMTQ0LjE5OCAxNDQuMTk4IDAgMCAwLTE3Ljc4LTE2OS4xOTJ6TTU0Mi4wMjIgNzYwLjk5NWgtMC4xNjNhMTA3LjE0OCAxMDcuMTQ4IDAgMCAxLTY4LjU2Mi0yNC44NTUgNjkuODU3IDY5Ljg1NyAwIDAgMCAzLjM3NS0xLjkzMmwxMTQuMDYtNjUuODYyYTE4LjU0OCAxOC41NDggMCAwIDAgOS4zNzktMTYuMTI4di0xNjAuOTNsNDguMjIgMjcuODMzYTEuNzIyIDEuNzIyIDAgMCAxIDAuOTMyIDEuMzI3djEzMy4xOWExMDcuNDk3IDEwNy40OTcgMCAwIDEtMTA3LjI0MSAxMDcuMzU3eiBtLTIzMC42OC05OC41MTRhMTA3LjE0OCAxMDcuMTQ4IDAgMCAxLTEyLjgtNzEuOTM2bDMuMzk4IDIuMDAyIDExNC4wMzcgNjUuODg1YTE4LjU5NSAxOC41OTUgMCAwIDAgMTguNzU4IDBsMTM5LjI2NC04MC40MDd2NTUuNzg0YTEuNzQ1IDEuNzQ1IDAgMCAxLTAuNjk5IDEuMzc0bC0xMTUuMjkzIDY2LjU2YTEwNy41NjcgMTA3LjU2NyAwIDAgMS0xMDcuMzM0IDAgMTA3LjQ5NyAxMDcuNDk3IDAgMCAxLTM5LjMzLTM5LjI4NXogbS0yOS45OTgtMjQ5LjAxOGExMDYuOTg1IDEwNi45ODUgMCAwIDEgNTUuODc4LTQ3LjA4bC0wLjA0NyAzLjk1NnYxMzEuODRhMTguNTI1IDE4LjUyNSAwIDAgMCA5LjM1NiAxNi4xMDVsMTM5LjI2NCA4MC40MDctNDguMjIxIDI3LjgzNGExLjc0NSAxLjc0NSAwIDAgMS0xLjYzIDAuMTRsLTExNS4zMTYtNjYuNjNhMTA3LjQ5NyAxMDcuNDk3IDAgMCAxLTM5LjI4NC0xNDYuNTk1eiBtMzk2LjEwMiA5Mi4xNmwtMTM5LjI0LTgwLjM4NCA0OC4xOTctMjcuODM0YTEuNzIyIDEuNzIyIDAgMCAxIDEuNjI5LTAuMTYzbDExNS4zMTYgNjYuNTgzYTEwNy40MjcgMTA3LjQyNyAwIDAgMS0xNi41OTMgMTkzLjc0NlY1MjEuNzA0YTE4LjUyNSAxOC41MjUgMCAwIDAtOS4zMS0xNi4wNTd6IG00Ny45ODgtNzIuMjE1YTE3MS4wNTUgMTcxLjA1NSAwIDAgMC0zLjM3NC0yLjAyNUw2MDggMzY1LjUyMWExOC42MTggMTguNjE4IDAgMCAwLTE4Ljc1OCAwbC0xMzkuMjQgODAuMzg0di01NS43NjFjMC0wLjUzNSAwLjIzMi0xLjA3IDAuNjk4LTEuMzk2bDExNS4yOTMtNjYuNTE0YTEwNy4zOCAxMDcuMzggMCAwIDEgMTU5LjQ0MSAxMTEuMTc0eiBtLTMwMS42MzggOTkuMjM1bC00OC4yMi0yNy44MzRhMS42OTkgMS42OTkgMCAwIDEtMC45MzItMS4zMjdWMzcwLjMxNmExMDcuMzggMTA3LjM4IDAgMCAxIDE3Ni4wNTktODIuNDU2IDk3LjEzNSA5Ny4xMzUgMCAwIDAtMy4zNzUgMS45MzJsLTExNC4wODMgNjUuODg1YTE4LjU3MiAxOC41NzIgMCAwIDAtOS4zNTYgMTYuMTA1djAuMTE2bC0wLjA5MyAxNjAuNzQ1eiBtMjYuMjA1LTU2LjQ2TDUxMiA0NDAuMzQzbDYyLjAyMiAzNS44MTd2NzEuNjMzTDUxMiA1ODMuNTg3bC02Mi4wMjItMzUuNzk0VjQ3Ni4xNnoiIGZpbGw9IiNGRkZGRkYiIHAtaWQ9IjE0MDU3Ij48L3BhdGg+PC9zdmc+',
    time: '2024-07-04 18:27:00',
    desc: 'The OpenAI ChatGPT 3.5 model is a powerful conversational AI model that can generate human-like responses to text inputs.',
    inner: true,
    systemPrompt: '',
    model: 'gpt-3.5-turbo',
    contentRows: 3,
    plugin: []
  },
  {
    id: '-2',
    name: 'ChatGPT 4',
    lastContent: 'Not message yet.',
    avatar: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNzE5OTgwNTczODkzIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE0MDU1IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxwYXRoIGQ9Ik04MTUuNjE2IDBIMjA4LjM4NEM5My4zIDAgMCA5NC4wNjggMCAyMTAuMDgzdjYwMy44MzRDMCA5MjkuOTU1IDkzLjMgMTAyNCAyMDguMzg0IDEwMjRoNjA3LjIzMkM5MzAuNyAxMDI0IDEwMjQgOTI5Ljk1NSAxMDI0IDgxMy45MTdWMjEwLjA4M0MxMDI0IDk0LjA2OCA5MzAuNyAwIDgxNS42MTYgMHoiIGZpbGw9IiMxMEEzN0YiIHAtaWQ9IjE0MDU2Ij48L3BhdGg+PHBhdGggZD0iTTc1Ny4zNjQgNDYwLjAzMkExNDIuODI1IDE0Mi44MjUgMCAwIDAgNzQ1LjEgMzQyLjgwN2ExNDQuNDA3IDE0NC40MDcgMCAwIDAtMTU1LjQ2Mi02OS4yNiAxNDIuNzA4IDE0Mi43MDggMCAwIDAtMTA2LjcyOS00Ny45ODhoLTEuMjU3YTE0NC4zODQgMTQ0LjM4NCAwIDAgMC0xMzcuMzU1IDk5LjkzMyAxNDIuNzU1IDE0Mi43NTUgMCAwIDAtOTUuNDE4IDY5LjIzNyAxNDQuNDMgMTQ0LjQzIDAgMCAwIDE3Ljc1NyAxNjkuMjYyIDE0Mi44MjUgMTQyLjgyNSAwIDAgMCAxMi4yNDEgMTE3LjIwMiAxNDQuMzg0IDE0NC4zODQgMCAwIDAgMTU1LjQ2MiA2OS4yNkExNDIuNzU1IDE0Mi43NTUgMCAwIDAgNTQxLjA5IDc5OC40NGgxLjI4YTE0NC4zMzcgMTQ0LjMzNyAwIDAgMCAxMzcuMzU2LTEwMC4wMDMgMTQyLjczMiAxNDIuNzMyIDAgMCAwIDk1LjQxOC02OS4yMzcgMTQ0LjE5OCAxNDQuMTk4IDAgMCAwLTE3Ljc4LTE2OS4xOTJ6TTU0Mi4wMjIgNzYwLjk5NWgtMC4xNjNhMTA3LjE0OCAxMDcuMTQ4IDAgMCAxLTY4LjU2Mi0yNC44NTUgNjkuODU3IDY5Ljg1NyAwIDAgMCAzLjM3NS0xLjkzMmwxMTQuMDYtNjUuODYyYTE4LjU0OCAxOC41NDggMCAwIDAgOS4zNzktMTYuMTI4di0xNjAuOTNsNDguMjIgMjcuODMzYTEuNzIyIDEuNzIyIDAgMCAxIDAuOTMyIDEuMzI3djEzMy4xOWExMDcuNDk3IDEwNy40OTcgMCAwIDEtMTA3LjI0MSAxMDcuMzU3eiBtLTIzMC42OC05OC41MTRhMTA3LjE0OCAxMDcuMTQ4IDAgMCAxLTEyLjgtNzEuOTM2bDMuMzk4IDIuMDAyIDExNC4wMzcgNjUuODg1YTE4LjU5NSAxOC41OTUgMCAwIDAgMTguNzU4IDBsMTM5LjI2NC04MC40MDd2NTUuNzg0YTEuNzQ1IDEuNzQ1IDAgMCAxLTAuNjk5IDEuMzc0bC0xMTUuMjkzIDY2LjU2YTEwNy41NjcgMTA3LjU2NyAwIDAgMS0xMDcuMzM0IDAgMTA3LjQ5NyAxMDcuNDk3IDAgMCAxLTM5LjMzLTM5LjI4NXogbS0yOS45OTgtMjQ5LjAxOGExMDYuOTg1IDEwNi45ODUgMCAwIDEgNTUuODc4LTQ3LjA4bC0wLjA0NyAzLjk1NnYxMzEuODRhMTguNTI1IDE4LjUyNSAwIDAgMCA5LjM1NiAxNi4xMDVsMTM5LjI2NCA4MC40MDctNDguMjIxIDI3LjgzNGExLjc0NSAxLjc0NSAwIDAgMS0xLjYzIDAuMTRsLTExNS4zMTYtNjYuNjNhMTA3LjQ5NyAxMDcuNDk3IDAgMCAxLTM5LjI4NC0xNDYuNTk1eiBtMzk2LjEwMiA5Mi4xNmwtMTM5LjI0LTgwLjM4NCA0OC4xOTctMjcuODM0YTEuNzIyIDEuNzIyIDAgMCAxIDEuNjI5LTAuMTYzbDExNS4zMTYgNjYuNTgzYTEwNy40MjcgMTA3LjQyNyAwIDAgMS0xNi41OTMgMTkzLjc0NlY1MjEuNzA0YTE4LjUyNSAxOC41MjUgMCAwIDAtOS4zMS0xNi4wNTd6IG00Ny45ODgtNzIuMjE1YTE3MS4wNTUgMTcxLjA1NSAwIDAgMC0zLjM3NC0yLjAyNUw2MDggMzY1LjUyMWExOC42MTggMTguNjE4IDAgMCAwLTE4Ljc1OCAwbC0xMzkuMjQgODAuMzg0di01NS43NjFjMC0wLjUzNSAwLjIzMi0xLjA3IDAuNjk4LTEuMzk2bDExNS4yOTMtNjYuNTE0YTEwNy4zOCAxMDcuMzggMCAwIDEgMTU5LjQ0MSAxMTEuMTc0eiBtLTMwMS42MzggOTkuMjM1bC00OC4yMi0yNy44MzRhMS42OTkgMS42OTkgMCAwIDEtMC45MzItMS4zMjdWMzcwLjMxNmExMDcuMzggMTA3LjM4IDAgMCAxIDE3Ni4wNTktODIuNDU2IDk3LjEzNSA5Ny4xMzUgMCAwIDAtMy4zNzUgMS45MzJsLTExNC4wODMgNjUuODg1YTE4LjU3MiAxOC41NzIgMCAwIDAtOS4zNTYgMTYuMTA1djAuMTE2bC0wLjA5MyAxNjAuNzQ1eiBtMjYuMjA1LTU2LjQ2TDUxMiA0NDAuMzQzbDYyLjAyMiAzNS44MTd2NzEuNjMzTDUxMiA1ODMuNTg3bC02Mi4wMjItMzUuNzk0VjQ3Ni4xNnoiIGZpbGw9IiNGRkZGRkYiIHAtaWQ9IjE0MDU3Ij48L3BhdGg+PC9zdmc+',
    time: '',
    desc: 'The OpenAI\'s ChatGPT 4 model is an even better conversational AI model, which supports more context than ChatGPT 3.5, and has a higher IQ.',
    inner: true,
    systemPrompt: '',
    model: 'gpt-4o',
    contentRows: 3,
    plugin: []
  },
  {
    id: '-3',
    name: 'Claude 3',
    lastContent: 'Not message yet.',
    avatar: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNzIwMDg4MzE3NTMyIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE1MTA2IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxwYXRoIGQ9Ik0wIDBoMTAyNHYxMDI0SDB6IiBmaWxsPSIjRDE5Qjc1IiBwLWlkPSIxNTEwNyI+PC9wYXRoPjxwYXRoIGQ9Ik0zMDMuODIxODc1IDc0Mi40SDIwNi4xODEyNWExLjM4MTI1IDEuMzgxMjUgMCAwIDEtMS4xNS0wLjY0MDYyNSAxLjQ1OTM3NSAxLjQ1OTM3NSAwIDAgMS0wLjEyODEyNS0xLjMzMTI1bDE3OC4xMjUtNDU3Ljk1OTM3NWExLjM4MTI1IDEuMzgxMjUgMCAwIDEgMS4zMDMxMjUtMC44Njg3NWgxMDEuOTkwNjI1YzAuNTYyNSAwIDEuMDc1IDAuMzMxMjUgMS4yODEyNSAwLjg2ODc1bDE3Ny44OTM3NSA0NTcuOTg0Mzc1YTEuNDU5Mzc1IDEuNDU5Mzc1IDAgMCAxLTAuMTI1IDEuMzA2MjUgMS4zODEyNSAxLjM4MTI1IDAgMCAxLTEuMTU2MjUgMC42NDA2MjVoLTk3LjQzMTI1YTEuMzgxMjUgMS4zODEyNSAwIDAgMS0xLjI4MTI1LTAuOTIxODc1bC0zNS44NjU2MjUtOTQuNTE1NjI1YTEuMzgxMjUgMS4zODEyNSAwIDAgMC0xLjI4MTI1LTAuOTIxODc1aC0xODYuMzEyNWExLjM4MTI1IDEuMzgxMjUgMCAwIDAtMS4yODEyNSAwLjkyMTg3NWwtMzUuNjg3NSA5NC41MTU2MjVhMS4zODEyNSAxLjM4MTI1IDAgMCAxLTEuMjgxMjUgMC45MjE4NzV6IG04MC4yODEyNS0xODEuMDQzNzVhMS4yODEyNSAxLjI4MTI1IDAgMCAwIDAuMTUzMTI1IDEuMjgxMjVjMC4yODEyNSAwLjM1NjI1IDAuNzQwNjI1IDAuNTYyNSAxLjIwMzEyNSAwLjU2MjVoMTI1LjA4MTI1YTEuNDg0Mzc1IDEuNDg0Mzc1IDAgMCAwIDEuMjAzMTI1LTAuNTg3NSAxLjI4MTI1IDEuMjgxMjUgMCAwIDAgMC4xNTYyNS0xLjI4MTI1bC02Mi41MTg3NS0xNTAuODM0Mzc1YTEuNDU5Mzc1IDEuNDU5Mzc1IDAgMCAwLTEuMzgxMjUtMC44OTY4NzUgMS40NTkzNzUgMS40NTkzNzUgMCAwIDAtMS4zODEyNSAwLjg5Njg3NXogbTE2MC4xNTMxMjUtMjc5Ljc1NjI1aDk1LjMwOTM3NWExLjUzNzUgMS41Mzc1IDAgMCAxIDEuNDU5Mzc1IDEuMDI1bDE3OC4wNzE4NzUgNDU3LjZhMS42OTY4NzUgMS42OTY4NzUgMCAwIDEtMC4xNTMxMjUgMS40ODQzNzUgMS41Mzc1IDEuNTM3NSAwIDAgMS0xLjI4MTI1IDAuNjg3NUg3MjIuNDA2MjVhMS41Mzc1IDEuNTM3NSAwIDAgMS0xLjQ4NDM3NS0xLjAyMTg3NUw1NDIuODIxODc1IDI4My43NzVhMS42MTI1IDEuNjEyNSAwIDAgMSAwLjE1NjI1LTEuNDg0Mzc1IDEuNTM3NSAxLjUzNzUgMCAwIDEgMS4yNzgxMjUtMC42OTA2MjV6IiBmaWxsPSIjMUYxRjFFIiBwLWlkPSIxNTEwOCI+PC9wYXRoPjwvc3ZnPg==',
    time: '',
    desc: 'Claude 3 is a powerful conversational AI model that can generate human-like responses to text inputs.',
    inner: true,
    systemPrompt: '',
    model: 'claude-3',
    contentRows: 3,
    plugin: []
  },
  {
    id: '-4',
    name: 'Claude 3.5',
    lastContent: 'Not message yet.',
    avatar: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNzIwMDg4MzE3NTMyIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE1MTA2IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxwYXRoIGQ9Ik0wIDBoMTAyNHYxMDI0SDB6IiBmaWxsPSIjRDE5Qjc1IiBwLWlkPSIxNTEwNyI+PC9wYXRoPjxwYXRoIGQ9Ik0zMDMuODIxODc1IDc0Mi40SDIwNi4xODEyNWExLjM4MTI1IDEuMzgxMjUgMCAwIDEtMS4xNS0wLjY0MDYyNSAxLjQ1OTM3NSAxLjQ1OTM3NSAwIDAgMS0wLjEyODEyNS0xLjMzMTI1bDE3OC4xMjUtNDU3Ljk1OTM3NWExLjM4MTI1IDEuMzgxMjUgMCAwIDEgMS4zMDMxMjUtMC44Njg3NWgxMDEuOTkwNjI1YzAuNTYyNSAwIDEuMDc1IDAuMzMxMjUgMS4yODEyNSAwLjg2ODc1bDE3Ny44OTM3NSA0NTcuOTg0Mzc1YTEuNDU5Mzc1IDEuNDU5Mzc1IDAgMCAxLTAuMTI1IDEuMzA2MjUgMS4zODEyNSAxLjM4MTI1IDAgMCAxLTEuMTU2MjUgMC42NDA2MjVoLTk3LjQzMTI1YTEuMzgxMjUgMS4zODEyNSAwIDAgMS0xLjI4MTI1LTAuOTIxODc1bC0zNS44NjU2MjUtOTQuNTE1NjI1YTEuMzgxMjUgMS4zODEyNSAwIDAgMC0xLjI4MTI1LTAuOTIxODc1aC0xODYuMzEyNWExLjM4MTI1IDEuMzgxMjUgMCAwIDAtMS4yODEyNSAwLjkyMTg3NWwtMzUuNjg3NSA5NC41MTU2MjVhMS4zODEyNSAxLjM4MTI1IDAgMCAxLTEuMjgxMjUgMC45MjE4NzV6IG04MC4yODEyNS0xODEuMDQzNzVhMS4yODEyNSAxLjI4MTI1IDAgMCAwIDAuMTUzMTI1IDEuMjgxMjVjMC4yODEyNSAwLjM1NjI1IDAuNzQwNjI1IDAuNTYyNSAxLjIwMzEyNSAwLjU2MjVoMTI1LjA4MTI1YTEuNDg0Mzc1IDEuNDg0Mzc1IDAgMCAwIDEuMjAzMTI1LTAuNTg3NSAxLjI4MTI1IDEuMjgxMjUgMCAwIDAgMC4xNTYyNS0xLjI4MTI1bC02Mi41MTg3NS0xNTAuODM0Mzc1YTEuNDU5Mzc1IDEuNDU5Mzc1IDAgMCAwLTEuMzgxMjUtMC44OTY4NzUgMS40NTkzNzUgMS40NTkzNzUgMCAwIDAtMS4zODEyNSAwLjg5Njg3NXogbTE2MC4xNTMxMjUtMjc5Ljc1NjI1aDk1LjMwOTM3NWExLjUzNzUgMS41Mzc1IDAgMCAxIDEuNDU5Mzc1IDEuMDI1bDE3OC4wNzE4NzUgNDU3LjZhMS42OTY4NzUgMS42OTY4NzUgMCAwIDEtMC4xNTMxMjUgMS40ODQzNzUgMS41Mzc1IDEuNTM3NSAwIDAgMS0xLjI4MTI1IDAuNjg3NUg3MjIuNDA2MjVhMS41Mzc1IDEuNTM3NSAwIDAgMS0xLjQ4NDM3NS0xLjAyMTg3NUw1NDIuODIxODc1IDI4My43NzVhMS42MTI1IDEuNjEyNSAwIDAgMSAwLjE1NjI1LTEuNDg0Mzc1IDEuNTM3NSAxLjUzNzUgMCAwIDEgMS4yNzgxMjUtMC42OTA2MjV6IiBmaWxsPSIjMUYxRjFFIiBwLWlkPSIxNTEwOCI+PC9wYXRoPjwvc3ZnPg==',
    time: '',
    desc: 'The new Claude 3.5 model surpasses ChatGPT 4 to some extent.',
    inner: true,
    systemPrompt: '',
    model: 'claude-3.5',
    contentRows: 3,
    plugin: []
  },
  {
    id: '-5',
    name: 'Gemini Pro',
    lastContent: 'Not message yet.',
    avatar: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNzIwMDg4NzM5MDM4IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjIxNDA4IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxwYXRoIGQ9Ik0yMTQuMTAxMzMzIDUxMmMwLTMyLjUxMiA1LjU0NjY2Ny02My43MDEzMzMgMTUuMzYtOTIuOTI4TDU3LjE3MzMzMyAyOTAuMjE4NjY3QTQ5MS44NjEzMzMgNDkxLjg2MTMzMyAwIDAgMCA0LjY5MzMzMyA1MTJjMCA3OS43MDEzMzMgMTguODU4NjY3IDE1NC44OCA1Mi4zOTQ2NjcgMjIxLjYxMDY2N2wxNzIuMjAyNjY3LTEyOS4wNjY2NjdBMjkwLjU2IDI5MC41NiAwIDAgMSAyMTQuMTAxMzMzIDUxMiIgZmlsbD0iI0ZCQkMwNSIgcC1pZD0iMjE0MDkiPjwvcGF0aD48cGF0aCBkPSJNNTE2LjY5MzMzMyAyMTYuMTkyYzcyLjEwNjY2NyAwIDEzNy4yNTg2NjcgMjUuMDAyNjY3IDE4OC40NTg2NjcgNjUuOTYyNjY3TDg1NC4xMDEzMzMgMTM2LjUzMzMzM0M3NjMuMzQ5MzMzIDU5LjE3ODY2NyA2NDYuOTk3MzMzIDExLjM5MiA1MTYuNjkzMzMzIDExLjM5MmMtMjAyLjMyNTMzMyAwLTM3Ni4yMzQ2NjcgMTEzLjI4LTQ1OS41MiAyNzguODI2NjY3bDE3Mi4zNzMzMzQgMTI4Ljg1MzMzM2MzOS42OC0xMTguMDE2IDE1Mi44MzItMjAyLjg4IDI4Ny4xNDY2NjYtMjAyLjg4IiBmaWxsPSIjRUE0MzM1IiBwLWlkPSIyMTQxMCI+PC9wYXRoPjxwYXRoIGQ9Ik01MTYuNjkzMzMzIDgwNy44MDhjLTEzNC4zNTczMzMgMC0yNDcuNTA5MzMzLTg0Ljg2NC0yODcuMjMyLTIwMi44OGwtMTcyLjI4OCAxMjguODUzMzMzYzgzLjI0MjY2NyAxNjUuNTQ2NjY3IDI1Ny4xNTIgMjc4LjgyNjY2NyA0NTkuNTIgMjc4LjgyNjY2NyAxMjQuODQyNjY3IDAgMjQ0LjA1MzMzMy00My4zOTIgMzMzLjU2OC0xMjQuNzU3MzMzbC0xNjMuNTg0LTEyMy44MTg2NjdjLTQ2LjEyMjY2NyAyOC40NTg2NjctMTA0LjIzNDY2NyA0My43NzYtMTcwLjAyNjY2NiA0My43NzYiIGZpbGw9IiMzNEE4NTMiIHAtaWQ9IjIxNDExIj48L3BhdGg+PHBhdGggZD0iTTEwMDUuMzk3MzMzIDUxMmMwLTI5LjU2OC00LjY5MzMzMy02MS40NC0xMS42NDgtOTEuMDA4SDUxNi42NTA2NjdWNjE0LjRoMjc0LjYwMjY2NmMtMTMuNjk2IDY1Ljk2MjY2Ny01MS4wNzIgMTE2LjY1MDY2Ny0xMDQuNTMzMzMzIDE0OS42MzJsMTYzLjU0MTMzMyAxMjMuODE4NjY3YzkzLjk5NDY2Ny04NS40MTg2NjcgMTU1LjEzNi0yMTIuNjUwNjY3IDE1NS4xMzYtMzc1Ljg1MDY2NyIgZmlsbD0iIzQyODVGNCIgcC1pZD0iMjE0MTIiPjwvcGF0aD48L3N2Zz4=',
    time: '',
    desc: 'Google\'s Gemini Pro model, which supports conversations and is faster.',
    inner: true,
    systemPrompt: '',
    model: 'gemini-1.5',
    contentRows: 3,
    plugin: []
  }
];


const initDefaultList = () => {
  let defaultChatString = localStorage.getItem('default_chat_list');
  if(!defaultChatString){
    defaultList.value = emptyList
  }else{
    try {
      defaultList.value = JSON.parse(defaultChatString)
    }catch (e) {
      defaultList.value = emptyList
    }
  }
  localStorage.setItem('default_chat_list', JSON.stringify(defaultList.value))
  // 遍历defaultList和emptyList，找出已删除的聊天和新增的聊天
  let deletedList = defaultList.value.filter(item => !emptyList.find(i => i.id === item.id))
  let addedList = emptyList.filter(item => !defaultList.value.find(i => i.id === item.id))
  // 如果有已删除的聊天，将其从defaultList中删除
  if(deletedList.length > 0){
    let newDefaultList = defaultList.value.filter(item => !deletedList.find(i => i.id === item.id))
    defaultList.value = newDefaultList
    localStorage.setItem('default_chat_list', JSON.stringify(defaultList.value))
    for (let i = 0; i < deletedList.length; i++) {
      localStorage.removeItem("message_list_" + deletedList[i].id)
    }
  }
  // 如果有新增的聊天，将其添加到defaultList中
  if(addedList.length > 0){
    defaultList.value = defaultList.value.concat(addedList)
    localStorage.setItem('default_chat_list', JSON.stringify(defaultList.value))
  }
}


//Customize

initDefaultList()
const useChat = ref(defaultList.value[0])

</script>

<template>
<div class="chat-view">
  <div class="left">
    <div class="header">
      <input type="text" placeholder="search" />
      <img src="../assets/search.svg" style="margin-right: 10px"/>
      <img src="../assets/add.svg"/>
    </div>
    <div class="chat-list">
      <div class="item" v-for="item in defaultList" :key="item.id" :class="{active: useChat === item}" @click="useChat = item">
        <img :src="item.avatar" alt="chat" />
        <div class="chat-item-center">
          <div class="name">{{item.name}}</div>
          <div class="content">{{item.lastContent}}</div>
        </div>
        <div class="chat-item-rigth">
          <div class="time">{{Util.getTimeSimpleString(item.time, '')}}</div>
        </div>
      </div>
    </div>
  </div>
  <div class="right">
    <chat-box :chat="useChat" :key="useChat.id"/>
  </div>
</div>
</template>

<style scoped lang="less">
.chat-view{
  display: flex;
  height: 100vh;
  .left{
    width: 270px;
    background-color: #ece3fd;
    .header{
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px;
      border-bottom: 1px solid #e0e0e0;
      input{
        width: 100%;
        height: 30px;
        border: none;
        padding: 0 10px;
        background: transparent;
        outline: none;
      }
      img{
        width: 20px;
        height: 20px;
        cursor: pointer;
        transition: opacity 0.3s;
        &:hover {
          opacity: 0.5;
        }
      }
    }

    .chat-list{
      height: calc(100vh - 60px);
      overflow-y: auto;
      .item{
        display: flex;
        padding: 10px;
        border-bottom: 1px solid #e4e4e4;
        cursor: default;
        transition: all 0.3s;
        img{
          width: 35px;
          height: 35px;
          border-radius: 50%;
        }
        .chat-item-center{
          flex: 1;
          padding-left: 10px;
          .name{
            font-size: 16px;
          }
          .content{
            font-size: 12px;
            color: #999;
          }
        }
        .chat-item-rigth{
          .time{
            font-size: 12px;
            color: #999;
          }
        }
        &:hover {
          background-color: #f7efff;
        }
        &.active{
          background-color: white;
          box-shadow: 0 5px 10px -5px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
  .right{
    flex: 1;
  }
}
</style>
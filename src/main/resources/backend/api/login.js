//登录请求的api
function loginApi(data) {
  return $axios({
    'url': '/employee/login',
    'method': 'post',
    data
  })
}

//退出登录请求的API
function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}

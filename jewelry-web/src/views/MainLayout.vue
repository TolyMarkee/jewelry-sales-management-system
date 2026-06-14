<template>
  <el-container style="height:100%">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h3>珠宝销售管理系统</h3>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9"
        active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <i class="el-icon-s-home" /><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/product">
          <i class="el-icon-goods" /><span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <i class="el-icon-menu" /><span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <i class="el-icon-s-order" /><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/customer">
          <i class="el-icon-user" /><span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/stock">
          <i class="el-icon-box" /><span>库存管理</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <i class="el-icon-s-data" /><span>销售统计</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <i class="el-icon-user-solid" /><span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="/user" v-if="isAdmin">
          <i class="el-icon-setting" /><span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span>{{ user ? user.realName || user.username : '' }}</span>
          <el-button type="text" @click="handleLogout" style="margin-left:20px;color:#f56c6c">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    <!-- AI 悬浮助手 -->
    <ai-assistant />
  </el-container>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import AiAssistant from '@/components/AiAssistant.vue'

export default {
  components: { AiAssistant },
  name: 'MainLayout',
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isAdmin']),
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    ...mapMutations(['logout']),
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
        this.logout()
        this.$router.push('/login')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.sidebar {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 50px;
}

.header-right {
  display: flex;
  align-items: center;
  color: #666;
}

.main-content {
  background: #f0f2f5;
  height: calc(100vh - 50px);
  overflow-y: auto;
}
</style>

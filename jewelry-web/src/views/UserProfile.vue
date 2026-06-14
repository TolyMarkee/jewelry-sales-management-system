<template>
  <div class="container">
    <el-row :gutter="20">
      <!-- 左侧：头像 -->
      <el-col :span="6">
        <el-card style="text-align:center">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="/api/upload/avatar"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload">
              <img v-if="form.avatar" :src="avatarUrl" class="avatar-img" />
              <i v-else class="el-icon-plus avatar-uploader-icon" />
            </el-upload>
            <h3 style="margin-top:15px">{{ form.realName || form.username }}</h3>
            <el-tag :type="form.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ form.role === 'admin' ? '管理员' : '销售员' }}
            </el-tag>
          </div>
        </el-card>

        <!-- 密码修改 -->
        <el-card style="margin-top:15px">
          <div slot="header"><span>修改密码</span></div>
          <el-form :model="pwdForm" ref="pwdFormRef" label-width="0" size="small">
            <el-form-item prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="原密码" show-password />
            </el-form-item>
            <el-form-item prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="新密码" show-password />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="确认新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" style="width:100%" @click="changePassword"
                :loading="pwdLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：基本信息 -->
      <el-col :span="18">
        <el-card>
          <div slot="header"><span>个人信息</span></div>
          <el-form :model="form" ref="profileForm" label-width="90px" size="small">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱地址" />
            </el-form-item>
            <el-form-item label="角色">
              <el-input :value="form.role === 'admin' ? '管理员' : '销售员'" disabled />
            </el-form-item>
            <el-form-item label="注册时间">
              <el-input :value="form.createTime" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saveLoading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getProfile, updateProfile, changePassword, uploadAvatar } from '@/api'
import { mapState, mapMutations } from 'vuex'

export default {
  name: 'UserProfile',
  data() {
    return {
      form: { id: null, username: '', realName: '', phone: '', email: '', avatar: '', role: '', createTime: '' },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      saveLoading: false,
      pwdLoading: false
    }
  },
  computed: {
    ...mapState(['user']),
    avatarUrl() {
      return this.form.avatar ? '/images/' + this.form.avatar : ''
    },
    uploadHeaders() {
      return { Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
    }
  },
  created() { this.loadProfile() },
  methods: {
    ...mapMutations(['setUser']),
    async loadProfile() {
      try {
        const res = await getProfile()
        this.form = res.data
      } catch (e) { /* 拦截器处理 */ }
    },
    async saveProfile() {
      this.saveLoading = true
      try {
        const res = await updateProfile({
          realName: this.form.realName,
          phone: this.form.phone,
          email: this.form.email,
          avatar: this.form.avatar
        })
        this.form = res.data
        // 同步更新 Vuex 中的用户信息
        this.setUser(res.data)
        this.$message.success('资料更新成功')
      } catch (e) { /* 拦截器处理 */ } finally {
        this.saveLoading = false
      }
    },
    async changePassword() {
      if (!this.pwdForm.oldPassword || !this.pwdForm.newPassword) {
        return this.$message.warning('请填写密码')
      }
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
        return this.$message.warning('两次新密码不一致')
      }
      this.pwdLoading = true
      try {
        await changePassword({
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        })
        this.$message.success('密码修改成功')
        this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
        // 清除登录状态，重新登录
        setTimeout(() => {
          this.$store.commit('logout')
          this.$router.push('/login')
        }, 1500)
      } catch (e) { /* 拦截器处理 */ } finally {
        this.pwdLoading = false
      }
    },
    handleAvatarSuccess(res) {
      if (res.code === 0 && res.data) {
        this.form.avatar = res.data
        this.saveProfile()
      } else {
        this.$message.error('头像上传失败')
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片文件'); return false }
      if (!isLt5M) { this.$message.error('图片大小不能超过 5MB'); return false }
      return true
    }
  }
}
</script>

<style scoped>
.avatar-section { padding: 10px 0; }
.avatar-uploader { display: inline-block; }
.avatar-uploader >>> .el-upload {
  border: 2px dashed #d9d9d9; border-radius: 50%; cursor: pointer;
  width: 120px; height: 120px; overflow: hidden;
}
.avatar-uploader >>> .el-upload:hover { border-color: #409EFF; }
.avatar-uploader-icon {
  font-size: 28px; color: #8c939d; width: 120px; height: 120px;
  line-height: 120px; text-align: center;
}
.avatar-img { width: 120px; height: 120px; border-radius: 50%; object-fit: cover; }
</style>

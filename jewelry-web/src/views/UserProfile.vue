<template>
  <div class="container-page">
    <div class="flex gap-5">
      <div class="w-64 shrink-0">
        <div class="bg-white rounded-xl p-6 text-center">
          <el-upload action="/api/upload/avatar" :headers="uploadHeaders" :show-file-list="false" :on-success="onAvatarSuccess" :before-upload="onAvatarBefore">
            <img v-if="form.avatar" :src="'/images/'+form.avatar" class="w-[100px] h-[100px] rounded-full object-cover mx-auto" />
            <div v-else class="w-[100px] h-[100px] rounded-full border-2 border-dashed border-gray-300 mx-auto flex items-center justify-center text-gray-400 cursor-pointer hover:border-blue-400"><el-icon :size="28"><Plus /></el-icon></div>
          </el-upload>
          <h3 class="mt-4 font-bold text-lg">{{ form.realName || form.username }}</h3>
          <el-tag :type="form.role==='admin'?'danger':'info'" size="small" class="mt-2">{{ form.role==='admin'?'管理员':'销售员' }}</el-tag>
        </div>
        <div class="bg-white rounded-xl p-5 mt-4">
          <h4 class="font-semibold mb-3">修改密码</h4>
          <el-form :model="pwd" ref="pwdRef" label-width="0" size="small">
            <el-form-item><el-input v-model="pwd.oldPassword" type="password" placeholder="原密码" show-password /></el-form-item>
            <el-form-item><el-input v-model="pwd.newPassword" type="password" placeholder="新密码" show-password /></el-form-item>
            <el-form-item><el-input v-model="pwd.confirmPassword" type="password" placeholder="确认新密码" show-password /></el-form-item>
            <el-form-item><el-button type="primary" size="small" class="w-full" @click="changePwd" :loading="pwdLoading">修改密码</el-button></el-form-item>
          </el-form>
        </div>
      </div>
      <div class="flex-1 bg-white rounded-xl p-6">
        <h4 class="font-semibold mb-5">个人信息</h4>
        <el-form :model="form" ref="profileRef" label-width="90px" size="small">
          <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="角色"><el-input :model-value="form.role==='admin'?'管理员':'销售员'" disabled /></el-form-item>
          <el-form-item label="注册时间"><el-input :model-value="form.createTime" disabled /></el-form-item>
          <el-form-item><el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button></el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, changePassword } from '@/api'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ id:null,username:'',realName:'',phone:'',email:'',avatar:'',role:'',createTime:'' })
const pwd = reactive({ oldPassword:'', newPassword:'', confirmPassword:'' }); const pwdRef = ref(null)
const saving = ref(false); const pwdLoading = ref(false)
const uploadHeaders = computed(() => ({ Authorization: 'Bearer '+(localStorage.getItem('token')||'') }))

onMounted(async () => { try { Object.assign(form, (await getProfile()).data) } catch {} })
async function saveProfile() { saving.value = true; try { const r = await updateProfile({ realName:form.realName, phone:form.phone, email:form.email, avatar:form.avatar }); Object.assign(form, r.data); userStore.setUser(r.data); ElMessage.success('更新成功') } catch {} finally { saving.value = false } }
async function changePwd() { if (!pwd.oldPassword||!pwd.newPassword) { ElMessage.warning('请填写密码'); return }; if (pwd.newPassword!==pwd.confirmPassword) { ElMessage.warning('两次密码不一致'); return }; pwdLoading.value = true; try { await changePassword({oldPassword:pwd.oldPassword,newPassword:pwd.newPassword}); ElMessage.success('密码修改成功'); Object.assign(pwd, { oldPassword:'',newPassword:'',confirmPassword:'' }); setTimeout(() => { userStore.logout(); router.push('/login') }, 1500) } catch {} finally { pwdLoading.value = false } }
function onAvatarSuccess(res) { if (res.code===0) { form.avatar = res.data; saveProfile() } else ElMessage.error('上传失败') }
function onAvatarBefore(file) { const ok = file.type.startsWith('image/') && file.size/1024/1024 < 5; if (!ok) ElMessage.error('仅图片且<5MB'); return ok }
</script>

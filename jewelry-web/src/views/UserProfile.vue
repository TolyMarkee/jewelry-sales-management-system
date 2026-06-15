<template>
  <div class="max-w-2xl mx-auto space-y-6">
    <h2 class="text-lg font-bold" style="color:var(--text-primary)">个人中心</h2>

    <!-- Avatar card -->
    <div class="rounded-2xl p-6 flex items-center gap-6" style="background:var(--surface); border:1px solid var(--border)">
      <div class="relative">
        <div class="w-20 h-20 rounded-full p-[3px]" style="background:linear-gradient(135deg,#a855f7,#6366f1,#a855f7)" />
        <el-upload ref="uploadRef" action="/api/upload/avatar" :headers="uploadHeaders" :show-file-list="false" :on-success="onAvatarSuccess" :before-upload="onAvatarBefore" class="absolute inset-0 flex items-center justify-center">
          <img v-if="form.avatar" :src="'/images/'+form.avatar" class="w-20 h-20 rounded-full object-cover cursor-pointer hover:opacity-80 transition-opacity" />
          <div v-else class="w-20 h-20 rounded-full flex items-center justify-center cursor-pointer text-2xl font-bold text-white" style="background:var(--bg)">{{ (form.realName||form.username||'?')[0] }}</div>
        </el-upload>
      </div>
      <div>
        <div class="text-lg font-bold" style="color:var(--text-primary)">{{ form.realName || form.username }}</div>
        <div class="text-xs mt-1" style="color:var(--text-muted)">{{ {admin:'管理员',manager:'销售主管',sales:'销售员'}[form.role] || '销售员' }} · {{ form.email || '未设置邮箱' }}</div>
        <button @click="triggerUpload" class="mt-2 px-3 py-1 rounded-lg text-xs font-medium transition-all hover:scale-105" style="background:rgba(168,85,247,0.15); color:var(--accent)">更换图片</button>
      </div>
    </div>

    <!-- Profile form -->
    <div class="rounded-2xl p-6 space-y-5" style="background:var(--surface); border:1px solid var(--border)">
      <div class="text-sm font-semibold mb-4" style="color:var(--text-primary)">基本信息</div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-xs mb-1.5" style="color:var(--text-muted)">用户名</label>
          <input v-model="form.username" disabled class="w-full h-10 px-3 rounded-lg text-sm outline-none" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-muted)" />
        </div>
        <div>
          <label class="block text-xs mb-1.5" style="color:var(--text-muted)">角色</label>
          <input :value="form.role==='admin'?'管理员':'销售员'" disabled class="w-full h-10 px-3 rounded-lg text-sm outline-none" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-muted)" />
        </div>
        <div>
          <label class="block text-xs mb-1.5" style="color:var(--text-muted)">真实姓名</label>
          <input v-model="form.realName" class="w-full h-10 px-3 rounded-lg text-sm outline-none transition-all focus:border-purple-500/50" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-primary)" />
        </div>
        <div>
          <label class="block text-xs mb-1.5" style="color:var(--text-muted)">手机号</label>
          <input v-model="form.phone" class="w-full h-10 px-3 rounded-lg text-sm outline-none transition-all focus:border-purple-500/50" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-primary)" />
        </div>
        <div class="col-span-2">
          <label class="block text-xs mb-1.5" style="color:var(--text-muted)">邮箱</label>
          <input v-model="form.email" class="w-full h-10 px-3 rounded-lg text-sm outline-none transition-all focus:border-purple-500/50" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-primary)" />
        </div>
      </div>
      <button @click="saveProfile" :disabled="saving" class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium transition-all hover:scale-105 disabled:opacity-50" style="background:linear-gradient(135deg,#a855f7,#6366f1); color:#fff">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
        {{ saving?'保存中...':'保存修改' }}
      </button>
    </div>

    <!-- Password -->
    <div class="rounded-2xl p-6 space-y-4" style="background:var(--surface); border:1px solid var(--border)">
      <div class="text-sm font-semibold" style="color:var(--text-primary)">修改密码</div>
      <div class="grid grid-cols-3 gap-4">
        <input v-model="pwd.oldPassword" type="password" placeholder="原密码" class="h-10 px-3 rounded-lg text-sm outline-none" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-primary)" />
        <input v-model="pwd.newPassword" type="password" placeholder="新密码" class="h-10 px-3 rounded-lg text-sm outline-none" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-primary)" />
        <input v-model="pwd.confirmPassword" type="password" placeholder="确认新密码" class="h-10 px-3 rounded-lg text-sm outline-none" style="background:var(--bg); border:1px solid var(--border-strong); color:var(--text-primary)" />
      </div>
      <button @click="changePwd" :disabled="pwdLoading" class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:rgba(180,83,9,0.1); color:var(--warning)">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        {{ pwdLoading?'修改中...':'修改密码' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, changePassword } from '@/api'

const router = useRouter(); const userStore = useUserStore()
const uploadRef = ref(null)
const form = reactive({ id:null,username:'',realName:'',phone:'',email:'',avatar:'',role:'',createTime:'' })
const pwd = reactive({ oldPassword:'',newPassword:'',confirmPassword:'' })
const saving = ref(false); const pwdLoading = ref(false)
const uploadHeaders = computed(()=>({Authorization:'Bearer '+(localStorage.getItem('token')||'')}))

onMounted(async()=>{try{Object.assign(form,(await getProfile()).data)}catch{}})
async function saveProfile(){saving.value=true;try{const r=await updateProfile({realName:form.realName,phone:form.phone,email:form.email,avatar:form.avatar});Object.assign(form,r.data);userStore.setUser(r.data);ElMessage.success('保存成功')}catch{}finally{saving.value=false}}
async function changePwd(){if(!pwd.oldPassword||!pwd.newPassword){ElMessage.warning('请填写完整');return};if(pwd.newPassword!==pwd.confirmPassword){ElMessage.warning('两次密码不一致');return};pwdLoading.value=true;try{await changePassword({oldPassword:pwd.oldPassword,newPassword:pwd.newPassword});ElMessage.success('密码已修改，即将重新登录');Object.assign(pwd,{oldPassword:'',newPassword:'',confirmPassword:''});setTimeout(()=>{userStore.logout();router.push('/login')},1500)}catch{}finally{pwdLoading.value=false}}
function onAvatarSuccess(res){if(res.code===0){form.avatar=res.data;saveProfile()}else ElMessage.error('上传失败')}
function onAvatarBefore(f){const ok=f.type.startsWith('image/')&&f.size/1024/1024<5;if(!ok)ElMessage.error('仅图片且<5MB');return ok}
function triggerUpload(){const el=uploadRef.value?.$el?.querySelector('input[type=file]');if(el)el.click()}
</script>

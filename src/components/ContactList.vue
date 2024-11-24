<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as XLSX from 'xlsx'

const contacts = ref([])
const searchQuery = ref('')
const dialogVisible = ref(false)
const currentContact = ref({
  name: '',
  phone: '',
  email: '',
  socialMedia: '',
  isFavorite: false
})

const loadContacts = async () => {
  try {
    const response = await axios.get('/api/contacts')
    contacts.value = response.data
  } catch (error) {
    ElMessage.error('加载联系人失败')
  }
}

const saveContact = async () => {
  if (!currentContact.value.name) {
    ElMessage.warning('请输入联系人姓名')
    return
  }
  try {
    if (currentContact.value.id) {
      await axios.put(`/api/contacts/${currentContact.value.id}`, currentContact.value)
    } else {
      await axios.post('/api/contacts', currentContact.value)
    }
    dialogVisible.value = false
    loadContacts()
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const toggleFavorite = async (contact) => {
  try {
    contact.isFavorite = !contact.isFavorite
    await axios.put(`/api/contacts/${contact.id}`, contact)
    ElMessage.success(contact.isFavorite ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    contact.isFavorite = !contact.isFavorite
    ElMessage.error('操作失败')
  }
}

const exportToExcel = () => {
  if (contacts.value.length === 0) {
    ElMessage.warning('没有可导出的联系人')
    return
  }
  const worksheet = XLSX.utils.json_to_sheet(contacts.value)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '联系人列表')
  XLSX.writeFile(workbook, '通讯录联系人.xlsx')
}

const importFromExcel = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = async (e) => {
    const data = new Uint8Array(e.target.result)
    const workbook = XLSX.read(data, { type: 'array' })
    const worksheet = workbook.Sheets[workbook.SheetNames[0]]
    const jsonData = XLSX.utils.sheet_to_json(worksheet)
    
    if (jsonData.length === 0) {
      ElMessage.warning('Excel文件中没有数据')
      return
    }
    
    try {
      await axios.post('/api/contacts/batch', jsonData)
      loadContacts()
      ElMessage.success('导入成功')
    } catch (error) {
      ElMessage.error('导入失败')
    }
  }
  reader.readAsArrayBuffer(file)
}

const editContact = (contact) => {
  currentContact.value = { ...contact }
  dialogVisible.value = true
}

const deleteContact = async (id) => {
  try {
    await axios.delete(`/api/contacts/${id}`)
    loadContacts()
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadContacts()
})
</script>

<template>
  <div class="contact-list">
    <div class="page-header">
      <h2>我的通讯录</h2>
    </div>
    
    <div class="controls">
      <el-input
        v-model="searchQuery"
        placeholder="搜索联系人（姓名、电话、邮箱）..."
        class="search-input"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <div class="action-buttons">
        <el-button type="primary" @click="dialogVisible = true; currentContact.value = {
          name: '',
          phone: '',
          email: '',
          socialMedia: '',
          isFavorite: false
        }">
          <el-icon><Plus /></el-icon>新建联系人
        </el-button>
        <el-button type="success" @click="exportToExcel">
          <el-icon><Download /></el-icon>导出Excel
        </el-button>
        <el-upload
          class="excel-upload"
          accept=".xlsx,.xls"
          :auto-upload="false"
          :show-file-list="false"
          @change="importFromExcel"
        >
          <el-button type="warning">
            <el-icon><Upload /></el-icon>导入Excel
          </el-button>
        </el-upload>
      </div>
    </div>

    <el-card class="contact-table" shadow="hover">
      <el-table 
        :data="contacts.filter(data => !searchQuery || 
          data.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          data.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
          data.phone.includes(searchQuery)
        )"
        style="width: 100%"
        :empty-text="'暂无联系人'"
        stripe
      >
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="电话" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="socialMedia" label="社交账号" min-width="150" />
        <el-table-column label="收藏" width="80" align="center">
          <template #default="{ row }">
            <el-button
              :type="row.isFavorite ? 'warning' : 'info'"
              @click="toggleFavorite(row)"
              circle
            >
              <el-icon>
                <Star v-if="row.isFavorite" />
                <StarFilled v-else />
              </el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" @click="editContact(row)" size="small">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-popconfirm
                title="确定要删除这个联系人吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="deleteContact(row.id)"
              >
                <template #reference>
                  <el-button type="danger" size="small">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="currentContact.id ? '编辑联系人' : '新建联系人'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="currentContact" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="currentContact.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="currentContact.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="currentContact.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="社交账号">
          <el-input v-model="currentContact.socialMedia" placeholder="请输入社交账号" />
        </el-form-item>
        <el-form-item label="收藏">
          <el-switch
            v-model="currentContact.isFavorite"
            active-text="已收藏"
            inactive-text="未收藏"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveContact">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.contact-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-header h2 {
  color: var(--el-text-color-primary);
  font-size: 28px;
  margin: 0;
}

.controls {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.search-input {
  width: 300px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.contact-table {
  margin-top: 20px;
}

.excel-upload {
  display: inline-block;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-upload) {
  width: auto;
}

.el-button-group {
  display: flex;
  gap: 5px;
}

:deep(.el-card__body) {
  padding: 0;
}

:deep(.el-table) {
  --el-table-border-color: var(--el-border-color-lighter);
}

:deep(.el-table th) {
  background-color: var(--el-fill-color-light);
}
</style> 
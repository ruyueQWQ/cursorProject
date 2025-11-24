<script setup>
defineProps({
  message: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div class="bubble" :class="message.role">
    <div class="bubble-meta">
      <span>{{ message.role === 'user' ? '学生' : '助教' }}</span>
      <small>{{ message.timestamp }}</small>
    </div>
    <p v-html="message.content" />
  </div>
</template>

<style scoped>
.bubble {
  padding: 1rem 1.25rem;
  border-radius: 1rem;
  max-width: 90%;
  background: #f2f5ff;
  align-self: flex-start;
}
.bubble.user {
  background: #e0f2fe;
  align-self: flex-end;
}
.bubble-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.4rem;
  font-size: 0.8rem;
  color: #64748b;
}
p {
  margin: 0;
  white-space: pre-wrap;
}
</style>


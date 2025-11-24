<script setup>
defineProps({
  reference: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <article class="reference-card">
    <header>
      <span class="badge">Score {{ reference.score.toFixed(2) }}</span>
      <h4>{{ reference.topicTitle }}</h4>
    </header>
    <p>{{ reference.snippet }}</p>
  </article>
</template>

<style scoped>
.reference-card {
  border: 1px solid #eef1ff;
  border-radius: 0.9rem;
  padding: 0.9rem 1rem;
  margin-bottom: 0.85rem;
  background: #fbfcff;
}
.reference-card h4 {
  margin: 0.2rem 0;
  font-size: 1rem;
  color: #1f2a44;
}
.reference-card p {
  margin: 0;
  color: #475569;
  font-size: 0.95rem;
}
.badge {
  font-size: 0.75rem;
  background: #e0f2fe;
  color: #0284c7;
  padding: 0.1rem 0.6rem;
  border-radius: 999px;
}
</style>


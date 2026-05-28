<template>
  <div class="emoji-picker" v-if="show" @click.stop>
    <div class="emoji-grid">
      <span v-for="e in emojis" :key="e" class="emoji-item" @click="pick(e)">{{ e }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ show: Boolean })
const emit = defineEmits(['pick', 'close'])

const emojis = [
  '😀','😃','😄','😁','😆','😅','🤣','😂','🙂','😉','😊','😇','🥰','😍','🤩','😘',
  '😜','😝','😛','🤑','🤗','🤭','🤫','🤔','🤐','🤨','😐','😑','😏','😒','🙄','😬',
  '😌','😔','😪','🤤','😴','😷','🤒','🤕','🤢','🤮','🥴','😵','🤯','🥳','😎','🧐',
  '😢','😭','😤','😠','😡','🤬','😈','👿','💀','☠️','💩','🤡','👹','👺','👻','👽',
  '👏','👍','👎','👊','✊','🤛','🤜','🤞','✌️','🤟','🤘','👌','❤️','🧡','💛','💚',
  '💙','💜','🖤','💔','💕','💞','💗','💖','✨','🔥','⭐','🌟','💫','🎉','🎊','🎈',
  '🐶','🐱','🐭','🐹','🐰','🦊','🐻','🐼','🐨','🐯','🦁','🐮','🐷','🐸','🐵','🐔',
  '🍎','🍐','🍊','🍋','🍌','🍉','🍇','🍓','🫐','🍈','🍒','🍑','🥭','🍍','🥥','🥝',
  '🍔','🍟','🍕','🌭','🥪','🌮','🌯','🥙','🥗','🍿','🥟','🍱','🍣','🍤','🍙','🍚',
  '⚽','🏀','🏈','⚾','🎾','🏐','🏉','🎱','🏓','🏸','🏒','🏑','🥅','🏏','⛳','🏹',
  '🚗','🚕','🚙','🚌','🚎','🏎️','🚓','🚑','🚒','🚐','🚚','🚛','🚜','🛴','🚲','🛵',
  '✈️','🚁','🚀','🛸','🚢','🛶','⛵','🚤','🛳️','🚉','🚄','🚅','🚃','🚇','🚌','🚲',
  '🏠','🏡','🏢','🏣','🏤','🏥','🏦','🏨','🏩','🏪','🏫','🏬','🏭','🏯','🏰','💒',
  '🌹','🥀','🌺','🌸','🌼','🌻','🌞','🌝','🌛','🌜','🌚','🌕','🌖','🌗','🌘','🌑',
  '🎵','🎶','🎸','🎺','🎻','🥁','🎹','🎤','🎧','🎼','🎬','🎨','🎭','🎪','🎟️','🎫'
]

const pick = (e) => {
  emit('pick', e)
  emit('close')
}
</script>

<style scoped>
.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  width: 280px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
  padding: 8px;
  border: 1px solid #eee;
}
.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.emoji-item {
  cursor: pointer;
  font-size: 22px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: 0.15s;
}
.emoji-item:hover {
  background: #fff0f3;
  transform: scale(1.2);
}
</style>

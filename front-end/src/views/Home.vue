<template>
  <div class="home">
    <div class="container">
      <!-- Navbar -->
      <Navbar></Navbar>
      <!-- End Navbar -->

      <!-- Heading -->
      <div>
        <h2 class="text-center" style="margin-top: 10px;">Data Lineage Visualisation</h2>
        <p class="lead text-uppercase text-center" style="font-size: 16px;">BY | SIVT1</p>
      </div>
      <!-- End Heading -->


      <!-- SQL Input -->
      <SQLInput v-if="!getisUploaded"></SQLInput>
      <!-- End SQL Input -->


      <!-- Force Directed Graph -->
      <ForceGraph v-if="getisUploaded"></ForceGraph>
      <!-- End Force Directed Graph -->
    </div>
    

    <!-- Footer -->
    <Footer></Footer>
    <!-- End Footer -->
  </div>
</template>

<script>
  import Navbar from '@/components/navbar/Navbar.vue'
  import SQLInput from '@/components/sqlInput/SQLInput.vue'
  import ForceGraph from '@/components/forceGraph/ForceGraph.vue'
  import Footer from '@/components/footer/Footer.vue'

  import {mapGetters} from 'vuex'

  export default {
    name: 'Home',
    data(){
      return {
        loading: false
      }
    },
    computed: {
      ...mapGetters(['getisUploaded'])
    },
    components: {
      Navbar,
      SQLInput,
      ForceGraph,
      Footer
    }
  }
</script>

<style>
</style>
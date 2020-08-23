<template>
    <div class="navbar">
        <!-- Navbar -->
        <nav class="navbar navbar-light navbar-expand-md">
            <div class="container-fluid">
                <a class="navbar-brand" href="#">Propic</a>
                <button data-toggle="collapse" class="navbar-toggler" data-target="#navcol-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navcol-1">
                    <ul class="nav navbar-nav ml-auto">
                        <li class="nav-item" role="presentation"><a class="nav-link active" href="#">Home</a></li>
                        <li class="nav-item" role="presentation">
                            <router-link to="/about" tag="li"><a class="nav-link">blabla</a></router-link>
                        </li>
                        <li class="nav-item" role="presentation"></li>
                    </ul>
                </div>
            </div>
        </nav>
        <!-- End Navbar -->
    </div>
</template>

<script>
    export default {
        name: 'Navbar'
    }
</script>

<style scoped>

</style>
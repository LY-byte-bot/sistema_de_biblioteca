const DemoToolbar = {
    props: ['currentView'],
    emits: ['navegar'],
    data: () => ({
        vistas: [
            { id: 'login', label: '7.1 Login' },
            { id: 'registro', label: '7.2 Registro' },
            { id: 'catalogo', label: '7.3 Catálogo' },
            { id: 'detalle', label: '7.4 Info Libro' },
            { id: 'prestamos', label: '7.5 Préstamos' },
            { id: 'admin-prestamos', label: '7.6 Admin WhatsApp' },
            { id: 'admin-catalogacion', label: '7.7 Admin Catálogo' },
            { id: 'admin-telemetria', label: '7.8 Telemetría' }
        ]
    }),
    template: `
    <nav class="navbar navbar-dark bg-dark py-1 px-3 d-print-none text-white small">
        <div class="container-fluid d-flex justify-content-between align-items-center">
            <span class="text-warning fw-semibold"><i class="bi bi-mortarboard-fill me-1"></i> UTP - Demostración de Maquetación:</span>
            <div class="btn-group btn-group-sm">
                <button v-for="v in vistas" :key="v.id" @click="$emit('navegar', v.id)" 
                        class="btn btn-outline-light py-0 px-2" :class="{ active: currentView === v.id }">
                    {{ v.label }}
                </button>
            </div>
        </div>
    </nav>`
};

// 2. Encabezado común para Lector y Administrador
const NavbarLector = {
    props: ['currentView', 'userName'],
    emits: ['navegar', 'logout'],
    template: `
    <header class="navbar-wire sticky-top d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center" @click="$emit('navegar', 'catalogo')" style="cursor: pointer;">
            <div class="brand-books"><span class="book-red"></span><span class="book-yellow"></span><span class="book-green"></span><span class="book-blue"></span></div>
            <span class="fw-bold fs-5 text-dark">Biblioteca Académica del Sur</span>
        </div>
        <div class="d-flex gap-3">
            <a href="#" @click.prevent="$emit('navegar', 'catalogo')" class="nav-link-wire" :class="{ active: currentView === 'catalogo' || currentView === 'detalle' }">Catálogo de Libros</a>
            <a href="#" @click.prevent="$emit('navegar', 'prestamos')" class="nav-link-wire" :class="{ active: currentView === 'prestamos' }">Mis Préstamos y Multas</a>
        </div>
        <div class="d-flex align-items-center gap-3">
            <span class="small fw-semibold text-dark"><i class="bi bi-person-fill me-1"></i> {{ userName }}</span>
            <button @click="$emit('logout')" class="btn btn-sm btn-wire-outline">Cerrar Sesión</button>
        </div>
    </header>`
};

const NavbarAdmin = {
    props: ['currentView'],
    emits: ['navegar', 'logout'],
    template: `
    <header class="navbar-wire sticky-top d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center" @click="$emit('navegar', 'admin-prestamos')" style="cursor: pointer;">
            <div class="brand-books"><span class="book-red"></span><span class="book-yellow"></span><span class="book-green"></span><span class="book-blue"></span></div>
            <span class="fw-bold fs-5 text-dark">Panel Admin - Biblioteca Académica</span>
        </div>
        <div class="d-flex gap-2">
            <a href="#" @click.prevent="$emit('navegar', 'admin-prestamos')" class="nav-link-wire" :class="{ active: currentView === 'admin-prestamos' }">Control Préstamos &amp; WhatsApp</a>
            <a href="#" @click.prevent="$emit('navegar', 'admin-catalogacion')" class="nav-link-wire" :class="{ active: currentView === 'admin-catalogacion' }">Catalogación &amp; Gestión</a>
            <a href="#" @click.prevent="$emit('navegar', 'admin-telemetria')" class="nav-link-wire" :class="{ active: currentView === 'admin-telemetria' }">Telemetría Actuator</a>
        </div>
        <div class="d-flex align-items-center gap-3">
            <span class="small fw-semibold text-dark"><i class="bi bi-shield-lock-fill me-1 text-primary"></i> Bibliotecario</span>
            <button @click="$emit('logout')" class="btn btn-sm btn-wire-outline">Cerrar Sesión</button>
        </div>
    </header>`
};

// 3. Componente 7.1: Login
const VistaLogin = {
    emits: ['navegar', 'login'],
    data: () => ({ correo: 'ivan.mamani@gmail.com', password: '••••••••••••' }),
    template: `
    <div class="container py-5 d-flex justify-content-center">
        <div class="wire-card p-4 p-md-5 col-md-5 col-lg-4 text-center">
            <div class="d-flex justify-content-center align-items-center mb-2">
                <div class="brand-books"><span class="book-red"></span><span class="book-yellow"></span><span class="book-green"></span><span class="book-blue"></span></div>
                <h5 class="fw-bold mb-0">Biblioteca Académica del Sur</h5>
            </div>
            <p class="text-muted small mb-3">Plataforma Web de Gestión Bibliográfica y Préstamos</p>
            <div class="wire-header-line mb-4"></div>

            <form @submit.prevent="$emit('login', correo)" class="text-start">
                <div class="mb-3">
                    <label class="form-label fw-bold small">Correo Electrónico:</label>
                    <input type="email" v-model="correo" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-bold small">Contraseña:</label>
                    <input type="password" v-model="password" class="form-control" required>
                </div>
                <div class="d-flex justify-content-between mb-4 small">
                    <label><input type="checkbox" checked> Recordar sesión</label>
                    <a href="#" class="text-primary">¿Olvidaste tu contraseña?</a>
                </div>
                <button type="submit" class="btn btn-wire-blue py-2 w-100 mb-3">Iniciar Sesión &rarr;</button>
                <div class="text-center small">
                    ¿No tienes una cuenta de lector? <a href="#" @click.prevent="$emit('navegar', 'registro')" class="fw-bold text-primary">Regístrate aquí</a>
                </div>
            </form>
        </div>
    </div>`
};

// 4. Componente 7.2: Registro (Formulario automático compacto con campos 100% vacíos)
const VistaRegistro = {
    emits: ['navegar', 'registro'],
    data: () => ({
        form: { nombres: '', apellidos: '', dni: '', telefono: '', correo: '', password: '', confirmPassword: '', terminos: false },
        campos: [
            { key: 'nombres', label: 'Nombres:', ph: 'Ingresa tus nombres', col: 6 },
            { key: 'apellidos', label: 'Apellidos:', ph: 'Ingresa tus apellidos', col: 6 },
            { key: 'dni', label: 'DNI (Documento de Identidad):', ph: '8 dígitos', col: 6, max: 8 },
            { key: 'telefono', label: 'Número Telefónico:', ph: 'Ej. +51 987 654 321', col: 6 },
            { key: 'correo', label: 'Correo Electrónico:', ph: 'ejemplo@correo.com', col: 12, type: 'email' },
            { key: 'password', label: 'Crear Contraseña:', ph: 'Mínimo 6 caracteres', col: 6, type: 'password' },
            { key: 'confirmPassword', label: 'Confirmar Contraseña:', ph: 'Repite tu contraseña', col: 6, type: 'password' }
        ]
    }),
    template: `
    <div class="container py-5 d-flex justify-content-center">
        <div class="wire-card p-4 p-md-5 col-md-7 col-lg-6">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <h4 class="fw-bold mb-1">📝 Registro de Nuevo Lector</h4>
                    <p class="text-muted small mb-0">Crea tu cuenta para solicitar préstamos y reservar libros</p>
                </div>
                <button @click="$emit('navegar', 'login')" class="btn btn-sm btn-wire-outline">&larr; Ir al Login</button>
            </div>
            <div class="wire-header-line mb-4"></div>

            <form @submit.prevent="$emit('registro', form)">
                <div class="row g-3 mb-3">
                    <div v-for="c in campos" :key="c.key" :class="'col-md-' + c.col">
                        <label class="form-label fw-bold small">{{ c.label }}</label>
                        <input :type="c.type || 'text'" v-model="form[c.key]" :placeholder="c.ph" :maxlength="c.max" class="form-control" required>
                    </div>
                </div>
                <div class="form-check mb-4 small">
                    <input class="form-check-input" type="checkbox" v-model="form.terminos" id="regl" required>
                    <label class="form-check-label" for="regl">Declaro conocer el reglamento de circulación física y el cálculo de multas por día de mora.</label>
                </div>
                <button type="submit" class="btn btn-wire-green w-100 py-2">&check; Registrar Cuenta de Lector &rarr;</button>
            </form>
        </div>
    </div>`
};

// 5. Componente 7.3: Catálogo
const VistaCatalogo = {
    props: ['libros'],
    emits: ['ver-ficha', 'reservar-libro'],
    data: () => ({ query: '', catFiltro: 'Todas', dispFiltro: 'Todos' }),
    computed: {
        filtrados() {
            return (this.libros || []).filter(l => {
                if (this.catFiltro !== 'Todas') {
                    const c = l.categorias && l.categorias[0] ? l.categorias[0].nombre : '';
                    if (!c.toLowerCase().includes(this.catFiltro.toLowerCase())) return false;
                }
                if (this.dispFiltro === 'Disponibles' && l.stockDisponible <= 0) return false;
                if (this.dispFiltro === 'Agotados' && l.stockDisponible > 0) return false;
                if (this.query && !l.titulo.toLowerCase().includes(this.query.toLowerCase())) return false;
                return true;
            });
        }
    },
    template: `
    <div class="container py-4">
        <!-- Buscador compacto -->
        <div class="wire-card p-3 mb-4">
            <div class="row g-3 align-items-center mb-3">
                <div class="col-lg-8">
                    <label class="form-label fw-bold small mb-1">Búsqueda de Material Bibliográfico:</label>
                    <div class="input-group">
                        <input type="text" v-model="query" class="form-control" placeholder="Escribe un título, autor o palabra clave...">
                        <button class="btn btn-wire-yellow px-4" type="button"><i class="bi bi-search me-1"></i> Buscar</button>
                    </div>
                </div>
                <div class="col-lg-4">
                    <label class="form-label fw-bold small mb-1">Modalidad:</label>
                    <div class="d-flex gap-3 pt-1 small">
                        <label><input type="radio" name="m" checked> Estándar</label>
                        <label><input type="radio" name="m"> Full-Text (Contenido)</label>
                    </div>
                </div>
            </div>
            <div class="row g-3 pt-2 border-top">
                <div class="col-md-4">
                    <select v-model="catFiltro" class="form-select form-select-sm">
                        <option value="Todas">Todas las Categorías</option>
                        <option value="Ingeniería">Ingeniería de Sistemas</option>
                        <option value="Desarrollo Web">Desarrollo Web</option>
                        <option value="Bases de Datos">Bases de Datos</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <select class="form-select form-select-sm">
                        <option>2020 - 2026 (Recientes)</option>
                        <option>Todos los años</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <select v-model="dispFiltro" class="form-select form-select-sm">
                        <option value="Todos">Todos (Disponibles y Prestados)</option>
                        <option value="Disponibles">Solo con Copias Disponibles</option>
                        <option value="Agotados">Solo Agotados (Reservables)</option>
                    </select>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0">Resultados: <span class="text-primary">{{ filtrados.length }} libros</span></h5>
            <span class="text-muted small">Indexación activa sobre 1,420 documentos</span>
        </div>

        <!-- Grilla de Libros -->
        <div class="row g-4 mb-4">
            <div v-for="l in filtrados" :key="l.idLibro || l.isbn" class="col-md-6 col-lg-4">
                <div class="wire-card p-3 h-100 d-flex flex-column justify-content-between">
                    <div>
                        <div class="row g-2 mb-2">
                            <div class="col-5"><div class="book-cover-placeholder"><span class="cover-text">PORTADA DEL LIBRO</span></div></div>
                            <div class="col-7">
                                <h6 class="fw-bold text-dark mb-1">{{ l.titulo }}</h6>
                                <p class="text-muted small mb-1"><strong>Autor:</strong> {{ (l.autores && l.autores[0]) ? l.autores[0].nombres : 'Varios Autores' }}</p>
                                <p class="text-muted small mb-1"><strong>Cat:</strong> {{ (l.categorias && l.categorias[0]) ? l.categorias[0].nombre : 'General' }}</p>
                                <p class="text-muted small mb-0"><strong>ISBN:</strong> {{ l.isbn }}</p>
                            </div>
                        </div>
                        <div class="notice-box-blue py-1 px-2 my-2 small" style="font-size: 0.76rem;">
                            <strong>Coincidencia:</strong> "{{ l.resumen ? l.resumen.substring(0, 65) + '...' : 'Contenido indexado disponible.' }}"
                        </div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center pt-2 border-top mt-2">
                        <span :class="l.stockDisponible > 0 ? 'badge bg-success-subtle text-success border border-success' : 'badge bg-danger-subtle text-danger border border-danger'" class="rounded-pill px-2 py-1 small">
                            {{ l.stockDisponible > 0 ? l.stockDisponible + ' en Sala' : '0 Copias' }}
                        </span>
                        <button v-if="l.stockDisponible > 0" @click="$emit('ver-ficha', l)" class="btn btn-sm btn-wire-blue">Ver Ficha &amp; Pedir &rarr;</button>
                        <button v-else @click="$emit('reservar-libro', l)" class="btn btn-sm btn-wire-yellow">Reservar &rarr;</button>
                    </div>
                </div>
            </div>
        </div>
    </div>`
};

// 6. Componente 7.4: Detalle del Libro
const VistaDetalle = {
    props: ['selectedLibro'],
    emits: ['navegar', 'solicitar-prestamo', 'reservar-libro'],
    template: `
    <div class="container py-4">
        <div class="wire-card p-4 p-md-5">
            <button @click="$emit('navegar', 'catalogo')" class="btn btn-sm btn-wire-outline mb-3">&larr; Volver al Catálogo</button>
            <div class="d-flex justify-content-between align-items-start mb-4">
                <div>
                    <h3 class="fw-bold mb-1">{{ selectedLibro.titulo }}</h3>
                    <p class="text-muted mb-0">Por: Martin Fowler &amp; Rod Johnson</p>
                </div>
                <span class="badge bg-success-subtle text-success border border-success px-3 py-2 rounded-pill fs-6">
                    &check; {{ selectedLibro.stockDisponible || 3 }} Ejemplares Disponibles
                </span>
            </div>

            <div class="row g-4 mb-3">
                <div class="col-md-3"><div class="book-cover-placeholder large"><span class="cover-text">PORTADA DEL LIBRO</span></div></div>
                <div class="col-md-9">
                    <div class="wire-card p-3 mb-3 bg-light row g-2 small">
                        <div class="col-6"><strong>ISBN:</strong> {{ selectedLibro.isbn }}</div>
                        <div class="col-6"><strong>Año:</strong> {{ selectedLibro.anioPublicacion || 2024 }}</div>
                        <div class="col-6"><strong>Editorial:</strong> {{ selectedLibro.editorial || 'Addison-Wesley' }}</div>
                        <div class="col-6"><strong>Plazo:</strong> 7 días renovables</div>
                    </div>
                    <div class="mb-3">
                        <h6 class="fw-bold mb-1">Sinopsis:</h6>
                        <p class="text-secondary small">{{ selectedLibro.resumen }}</p>
                    </div>
                    <div class="notice-box-blue mb-3">
                        <h6 class="fw-bold mb-2"><i class="bi bi-bookmark-star me-1"></i> Temas Clave y Palabras Destacadas:</h6>
                        <ul class="mb-0 ps-3 small">
                            <li>Persistencia de datos y transacciones concurrentes con JPA y PostgreSQL.</li>
                            <li>Diseño de controladores RESTful limpios y consumo de servicios.</li>
                            <li>Arquitectura en capas, buenas prácticas de desarrollo y control de stock.</li>
                        </ul>
                    </div>
                    <div class="d-flex gap-3 mb-4">
                        <button @click="$emit('solicitar-prestamo', selectedLibro)" class="btn btn-wire-yellow py-2 px-4">📖 Solicitar Préstamo de Ejemplar Físico</button>
                        <button onclick="alert('Descargando muestra PDF...')" class="btn btn-wire-outline py-2 px-3">📄 Descargar Muestra (PDF)</button>
                    </div>
                    <div class="notice-box-yellow">
                        <strong><i class="bi bi-exclamation-triangle-fill text-warning me-1"></i> Nota sobre el Recojo Presencial:</strong>
                        El ejemplar se reservará por 24 horas. Deberás acercarte con tu DNI al mostrador de Atención al Usuario para retirar el libro.
                    </div>
                </div>
            </div>
        </div>
    </div>`
};

// 7. Componente 7.5: Mis Préstamos y Multas
const VistaPrestamos = {
    props: ['prestamosLector', 'devolucionesLector'],
    data: () => ({
        resumenes: [
            { titulo: 'Préstamos Activos', val: '2 Libros', sub: 'En posesión física', border: 'border-primary', color: 'text-primary' },
            { titulo: 'Multa Acumulada', val: 'S/. 6.00', sub: '⚠️ 1 libro con retraso', border: 'border-danger', color: 'text-danger' },
            { titulo: 'Devoluciones Cumplidas', val: '12 Libros', sub: 'Historial positivo', border: 'border-success', color: 'text-success' }
        ]
    }),
    template: `
    <div class="container py-4">
        <div class="row g-3 mb-4">
            <div v-for="r in resumenes" :key="r.titulo" class="col-md-4">
                <div class="wire-card p-3 border-2" :class="r.border">
                    <div class="small fw-bold text-uppercase mb-1" :class="r.color">{{ r.titulo }}</div>
                    <div class="fs-2 fw-bold" :class="r.color">{{ r.val }}</div>
                    <div class="text-muted small">{{ r.sub }}</div>
                </div>
            </div>
        </div>

        <div class="wire-card p-4 mb-4">
            <h5 class="fw-bold mb-3">📋 1. Panel de Préstamos Activos</h5>
            <table class="table table-bordered text-center align-middle small mb-0">
                <thead class="table-light"><tr><th>Código</th><th>Título</th><th>Límite</th><th>Estado</th><th>Mora</th><th>Acción</th></tr></thead>
                <tbody>
                    <tr v-for="p in prestamosLector" :key="p.codigo">
                        <td class="fw-bold">{{ p.codigo }}</td>
                        <td>{{ p.titulo }}</td>
                        <td>{{ p.fechaLimite }}</td>
                        <td><span :class="p.diasRetraso > 0 ? 'badge bg-warning text-dark' : 'badge bg-success'">{{ p.estado }}</span></td>
                        <td :class="p.diasRetraso > 0 ? 'text-danger fw-bold' : 'text-success fw-bold'">{{ p.mora }}</td>
                        <td><button class="btn btn-sm btn-outline-danger">Devolver en Biblioteca</button></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="wire-card p-4 mb-4">
            <h5 class="fw-bold mb-3">📜 2. Historial de Devoluciones Anteriores</h5>
            <table class="table table-bordered text-center align-middle small mb-0">
                <thead class="table-light"><tr><th>Código</th><th>Título</th><th>Fecha</th><th>Estado Físico</th><th>Multa</th><th>Estado Pago</th></tr></thead>
                <tbody>
                    <tr v-for="d in devolucionesLector" :key="d.codigo">
                        <td>{{ d.codigo }}</td><td>{{ d.titulo }}</td><td>{{ d.fechaDevolucion }}</td>
                        <td><span class="badge bg-success-subtle text-success border border-success">{{ d.estadoFisico }}</span></td>
                        <td>{{ d.multaAplicada }}</td>
                        <td><span class="badge bg-success">{{ d.estadoPago }}</span></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="notice-box-yellow">
            <strong><i class="bi bi-info-circle-fill me-1"></i> Información:</strong> Toda devolución de libros y pago de sanciones por retraso se realiza <strong>de forma física y presencial</strong> en el mostrador.
        </div>
    </div>`
};

// 8. Componente 7.6: Admin Préstamos & WhatsApp
const VistaAdminPrestamos = {
    props: ['prestamosAdmin'],
    data() {
        return {
            p: this.prestamosAdmin[0],
            msg: '🔔 RECORDATORIO BIBLIOTECA: Su préstamo venció. Multa acumulada: S/. 6.00. Acérquese a la biblioteca para devolver el ejemplar.',
            estadoFisico: 'Óptimo / Sin daños'
        };
    },
    methods: {
        enviarWA() {
            window.open('https://wa.me/51958123456?text=' + encodeURIComponent(this.msg), '_blank');
            alert('✓ Notificación enviada.');
        },
        confirmar() {
            this.p.estado = 'DEVUELTO';
            this.p.mora = 'S/. 0.00';
            this.p.isVencido = false;
            alert('✓ Devolución confirmada y lector liberado.');
        }
    },
    template: `
    <div class="container py-4">
        <div class="wire-card p-4 mb-4">
            <h5 class="fw-bold mb-3">Control de Préstamos y Estados de Circulación</h5>
            <table class="table table-bordered text-center align-middle small mb-0">
                <thead class="table-light"><tr><th>Código</th><th>Lector</th><th>Libro</th><th>Límite</th><th>Estado</th><th>Acciones</th></tr></thead>
                <tbody>
                    <tr v-for="item in prestamosAdmin" :key="item.codigo">
                        <td class="fw-bold">{{ item.codigo }}</td>
                        <td>{{ item.lector }}</td>
                        <td>{{ item.libro }}</td>
                        <td>{{ item.fechaLimite }}</td>
                        <td><span :class="item.isVencido ? 'badge bg-danger' : 'badge bg-success'">{{ item.estado }}</span></td>
                        <td>
                            <button @click="p = item" class="btn btn-sm btn-outline-success me-1"><i class="bi bi-whatsapp"></i> WhatsApp</button>
                            <button @click="confirmar" class="btn btn-sm btn-outline-secondary">Recibir</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="wire-card p-4 mb-4">
            <h6 class="fw-bold mb-3"><i class="bi bi-whatsapp text-success me-1"></i> Apartado de Notificación y Recordatorio por WhatsApp</h6>
            <div class="row g-3">
                <div class="col-md-5 p-3 bg-light rounded small border">
                    <div><strong>Destinatario:</strong> {{ p.lector }}</div>
                    <div><strong>Libro:</strong> {{ p.libro }}</div>
                    <div class="text-danger fw-bold">Multa: {{ p.mora }}</div>
                </div>
                <div class="col-md-7">
                    <textarea v-model="msg" class="form-control font-monospace small mb-2" rows="3"></textarea>
                    <button @click="enviarWA" class="btn btn-wire-green w-100 py-2 fw-bold"><i class="bi bi-send-fill me-1"></i> Enviar por WhatsApp</button>
                </div>
            </div>
        </div>

        <div class="wire-card p-4">
            <h6 class="fw-bold mb-3"><i class="bi bi-cash-coin text-primary me-1"></i> Registro de Devolución en Mostrador</h6>
            <div class="row g-3 align-items-end">
                <div class="col-md-6">
                    <label class="form-label small fw-bold">Estado Físico:</label>
                    <select v-model="estadoFisico" class="form-select form-select-sm"><option>Óptimo / Sin daños</option><option>Dañado</option></select>
                </div>
                <div class="col-md-3">
                    <div class="p-1 text-center border border-danger text-danger fw-bold rounded">{{ p.mora }} (Efectivo)</div>
                </div>
                <div class="col-md-3">
                    <button @click="confirmar" class="btn btn-wire-green w-100 py-1">&check; Confirmar Devolución</button>
                </div>
            </div>
        </div>
    </div>`
};

// 9. Componente 7.7: Admin Catalogación (Con tabla para Quitar/Eliminar Libros)
const VistaAdminCatalogacion = {
    props: ['libros'],
    emits: ['guardar-libro', 'eliminar-libro'],
    data: () => ({
        tab: 'formulario',
        nuevo: { titulo: 'Sistemas Distribuidos', autores: 'Andrew S. Tanenbaum', isbn: '978-0132392273', categoria: 'Redes', editorial: 'Pearson - 2023', stockTotal: 5, ubicacion: 'B-08', resumen: 'Texto sobre arquitectura de sistemas distribuidos...' },
        campos: [
            { key: 'titulo', label: 'Título:', col: 5 },
            { key: 'autores', label: 'Autor(es):', col: 4 },
            { key: 'isbn', label: 'ISBN:', col: 3 },
            { key: 'categoria', label: 'Categoría:', col: 4 },
            { key: 'editorial', label: 'Editorial y Año:', col: 4 },
            { key: 'stockTotal', label: 'Ejemplares:', col: 2, type: 'number' },
            { key: 'ubicacion', label: 'Ubicación:', col: 2 }
        ]
    }),
    template: `
    <div class="container py-4">
        <div class="d-flex gap-2 mb-4 p-2 bg-white rounded border">
            <button @click="tab = 'formulario'" class="btn flex-fill" :class="tab === 'formulario' ? 'btn-wire-blue' : 'btn-light'"><i class="bi bi-plus-circle me-1"></i> 1. Registrar Nuevo Libro</button>
            <button @click="tab = 'inventario'" class="btn flex-fill" :class="tab === 'inventario' ? 'btn-wire-blue' : 'btn-light'"><i class="bi bi-collection me-1"></i> 2. Inventario y Dar de Baja (Eliminar)</button>
        </div>

        <div v-if="tab === 'formulario'" class="wire-card p-4">
            <h5 class="fw-bold mb-3">Registro de Nuevo Libro</h5>
            <form @submit.prevent="$emit('guardar-libro', nuevo); tab = 'inventario';">
                <div class="row g-3 mb-3">
                    <div v-for="c in campos" :key="c.key" :class="'col-md-' + c.col">
                        <label class="form-label small fw-bold">{{ c.label }}</label>
                        <input :type="c.type || 'text'" v-model="nuevo[c.key]" class="form-control form-control-sm" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label small fw-bold">Sinopsis:</label>
                        <textarea v-model="nuevo.resumen" class="form-control form-control-sm" rows="2"></textarea>
                    </div>
                </div>
                <div class="p-3 border rounded bg-light mb-3 d-flex justify-content-between align-items-center">
                    <div><label class="small fw-bold">Archivo PDF para Búsqueda:</label><input type="file" class="form-control form-control-sm"></div>
                    <span class="small text-muted"><i class="bi bi-info-circle me-1"></i> Indexación automática al guardar.</span>
                </div>
                <button type="submit" class="btn btn-wire-blue py-2 px-4">💾 Guardar Libro</button>
            </form>
        </div>

        <div v-if="tab === 'inventario'" class="wire-card p-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0">Inventario de Libros (Dar de Baja)</h5>
                <button @click="tab = 'formulario'" class="btn btn-sm btn-wire-green"><i class="bi bi-plus-lg me-1"></i> Nuevo Libro</button>
            </div>
            <table class="table table-bordered text-center align-middle small mb-0">
                <thead class="table-light"><tr><th>#</th><th>Título</th><th>ISBN</th><th>Stock</th><th>Acción</th></tr></thead>
                <tbody>
                    <tr v-for="(l, i) in libros" :key="l.isbn || i">
                        <td>{{ i + 1 }}</td>
                        <td class="text-start fw-bold">{{ l.titulo }}</td>
                        <td><code>{{ l.isbn }}</code></td>
                        <td>{{ l.stockDisponible }} en sala</td>
                        <td><button @click="$emit('eliminar-libro', l)" class="btn btn-sm btn-danger fw-bold"><i class="bi bi-trash3-fill me-1"></i> Dar de Baja</button></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>`
};

// 10. Componente 7.8: Telemetría Actuator
const VistaAdminTelemetria = {
    props: ['telemetria'],
    data: () => ({
        cards: [
            { titulo: 'API REST Backend', val: 'STATUS: UP', sub: 'Tiempo: 14d 06h | JVM: Java 21', color: 'text-success' },
            { titulo: 'Base de Datos PostgreSQL', val: 'Pool: 18 / 50', sub: 'Latencia: 4ms | Supabase OK', color: 'text-primary' },
            { titulo: 'Motor de Búsqueda', val: '1,420 PDFs', sub: 'Índice 100% Sincronizado', color: 'text-dark' }
        ]
    }),
    template: `
    <div class="container py-4">
        <h5 class="fw-bold mb-3">Monitor de Telemetría (Actuator)</h5>
        <div class="row g-3 mb-4">
            <div v-for="c in cards" :key="c.titulo" class="col-md-4">
                <div class="wire-card p-3">
                    <div class="small text-muted fw-bold mb-1">{{ c.titulo }}</div>
                    <div class="fs-4 fw-bold mb-1" :class="c.color">{{ c.val }}</div>
                    <div class="text-muted small">{{ c.sub }}</div>
                </div>
            </div>
        </div>
        <div class="wire-card p-4">
            <h6 class="fw-bold mb-3">📊 Estadísticas de Circulación</h6>
            <div class="row g-3">
                <div class="col-md-7">
                    <table class="table table-bordered text-center align-middle small mb-0">
                        <thead class="table-light"><tr><th>Libro</th><th>Categoría</th><th>Préstamos</th></tr></thead>
                        <tbody>
                            <tr v-for="t in telemetria.masSolicitados" :key="t.libro"><td class="text-start">{{ t.libro }}</td><td>{{ t.categoria }}</td><td class="fw-bold text-primary">{{ t.prestamos }}</td></tr>
                        </tbody>
                    </table>
                </div>
                <div class="col-md-5 small p-3 bg-light rounded border">
                    <div class="d-flex justify-content-between mb-2"><span>Total Ejemplares:</span><strong>3,850 libros</strong></div>
                    <div class="d-flex justify-content-between mb-2"><span>Préstamos Activos:</span><strong class="text-primary">142 libros</strong></div>
                    <div class="d-flex justify-content-between mb-2"><span>Vencidos con Mora:</span><strong class="text-danger">8 pendientes</strong></div>
                    <div class="d-flex justify-content-between"><span>Multas en Caja:</span><strong class="text-dark fs-6">S/. 284.00</strong></div>
                </div>
            </div>
        </div>
    </div>`
};

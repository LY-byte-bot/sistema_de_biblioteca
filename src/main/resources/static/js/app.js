const { createApp, ref, computed, onMounted } = Vue;

const app = createApp({
    components: {
        'demo-toolbar': DemoToolbar,
        'navbar-lector': NavbarLector,
        'navbar-admin': NavbarAdmin,
        'vista-login': VistaLogin,
        'vista-registro': VistaRegistro,
        'vista-catalogo': VistaCatalogo,
        'vista-detalle': VistaDetalle,
        'vista-prestamos': VistaPrestamos,
        'vista-admin-prestamos': VistaAdminPrestamos,
        'vista-admin-catalogacion': VistaAdminCatalogacion,
        'vista-admin-telemetria': VistaAdminTelemetria
    },
    setup() {
        // ==========================================
        // ESTADO DE NAVEGACIÓN Y USUARIO
        // ==========================================
        const currentView = ref('catalogo');
        const currentUser = ref({
            id: 1,
            nombre: 'Ivan Mamani (Lector)',
            rol: 'Lector',
            correo: 'ivan.mamani@gmail.com',
            dni: '72330451',
            telefono: '+51 958 123 456'
        });

        // Libros y Detalle
        const libros = ref([]);
        const selectedLibro = ref(null);

        // Préstamos Lector (7.5)
        const prestamosLector = ref([
            {
                codigo: 'PR-2026-089',
                titulo: 'Patrones de Diseño de Software',
                autor: 'Erich Gamma (Gang of Four)',
                fechaPrestamo: '15/08/2026',
                fechaLimite: '22/08/2026',
                estado: 'Vencido (+3 días)',
                diasRetraso: 3,
                mora: 'S/. 6.00',
                tarifa: 'S/. 2.00 / día retraso'
            },
            {
                codigo: 'PR-2026-104',
                titulo: 'Arquitectura Web con Spring Boot 3',
                autor: 'Martin Fowler',
                fechaPrestamo: '26/08/2026',
                fechaLimite: '02/09/2026',
                estado: 'Al día (5 días restantes)',
                diasRetraso: 0,
                mora: 'S/. 0.00 (Sin mora)',
                tarifa: ''
            }
        ]);

        const devolucionesLector = ref([
            {
                codigo: 'PR-2026-045',
                titulo: 'Clean Code: Manual de Estilo Ágil',
                fechaDevolucion: '04/08/2026',
                estadoFisico: 'Óptimo [OK]',
                multaAplicada: 'S/. 0.00',
                estadoPago: 'Conforme'
            },
            {
                codigo: 'PR-2026-012',
                titulo: 'Ingeniería de Software: Un Enfoque Práctico',
                fechaDevolucion: '18/07/2026',
                estadoFisico: 'Óptimo [OK]',
                multaAplicada: 'S/. 4.00 (2 días retraso)',
                estadoPago: 'Cancelado en Caja Física'
            }
        ]);

        // Préstamos Admin (7.6)
        const prestamosAdmin = ref([
            {
                codigo: 'PR-2026-089',
                lector: 'Ivan Mamani Villanueva',
                contacto: 'DNI: 72330451 | +51 958 123 456',
                telefono: '+51958123456',
                libro: 'Patrones de Diseño de Software',
                ejemplar: 'Ejemplar #02 (Estante B-4)',
                fechaLimite: '22/08/2026',
                estado: 'VENCIDO (+3 Días Retraso)',
                mora: 'S/. 6.00',
                diasRetraso: 3,
                isVencido: true
            },
            {
                codigo: 'PR-2026-104',
                lector: 'Ivan Mamani Villanueva',
                contacto: 'DNI: 72330451 | +51 958 123 456',
                telefono: '+51958123456',
                libro: 'Arquitectura Web con Spring Boot 3',
                ejemplar: 'Ejemplar #01 (Estante A-12)',
                fechaLimite: '02/09/2026',
                estado: 'VIGENTE (Quedan 5 días)',
                mora: 'S/. 0.00',
                diasRetraso: 0,
                isVencido: false
            }
        ]);

        // Telemetría Actuator (7.8)
        const telemetria = ref({
            status: 'UP',
            uptime: '14d 06h 22m',
            jvm: 'JVM: Java 21 LTS',
            dbPool: '18 / 50',
            dbLatencia: '4ms',
            totalPdfs: '1,420 PDFs',
            indiceSincronizado: '100% Sincronizado',
            totalEjemplares: '3,850 libros físicos',
            prestamosActivos: 142,
            ejemplaresVencidos: 8,
            alertasWhatsappHoy: 6,
            multasRecaudadas: 'S/. 284.00',
            masSolicitados: [
                { libro: 'Arquitectura Web con Spring Boot 3', categoria: 'Software', prestamos: '28 veces' },
                { libro: 'Patrones de Diseño de Software', categoria: 'Ingeniería', prestamos: '22 veces' },
                { libro: 'Bases de Datos PostgreSQL', categoria: 'Bases de Datos', prestamos: '19 veces' }
            ]
        });

        // TRUCO MAESTRO: Mapeo del componente activo según currentView
        const activeComponent = computed(() => {
            const map = {
                'login': 'vista-login',
                'registro': 'vista-registro',
                'catalogo': 'vista-catalogo',
                'detalle': 'vista-detalle',
                'prestamos': 'vista-prestamos',
                'admin-prestamos': 'vista-admin-prestamos',
                'admin-catalogacion': 'vista-admin-catalogacion',
                'admin-telemetria': 'vista-admin-telemetria'
            };
            return map[currentView.value] || 'vista-catalogo';
        });

        // ==========================================
        // MÉTODOS Y ACCIONES
        // ==========================================
        const navegarA = (vista) => {
            currentView.value = vista;
            window.scrollTo({ top: 0, behavior: 'smooth' });
        };

        const handleLogin = (correo) => {
            if (correo.includes('admin') || correo.includes('biblioteca.edu.pe')) {
                currentUser.value.nombre = 'Omar Valencia (Admin)';
                currentUser.value.rol = 'Bibliotecario';
                navegarA('admin-prestamos');
            } else {
                currentUser.value.nombre = 'Ivan Mamani (Lector)';
                currentUser.value.rol = 'Lector';
                navegarA('catalogo');
            }
        };

        const handleRegistro = async (form) => {
            try {
                await axios.post('/api/usuarios/registro', {
                    nombre: `${form.nombres} ${form.apellidos}`,
                    correo: form.correo,
                    passwordHash: form.password,
                    rol: 'LECTOR',
                    estado: 'ACTIVO',
                    dni: form.dni,
                    telefono: form.telefono,
                    direccion: 'Arequipa, Perú'
                });
            } catch (e) {
                console.info('Registro registrado en frontend');
            }
            alert('¡Registro exitoso! Ya puedes iniciar sesión con tu cuenta de lector.');
            navegarA('login');
        };

        const cerrarSesion = () => {
            navegarA('login');
        };

        const cargarLibros = async () => {
            try {
                const res = await axios.get('/api/libros');
                if (res.data && res.data.length > 0) {
                    libros.value = res.data;
                } else {
                    libros.value = getLibrosDemo();
                }
            } catch (e) {
                libros.value = getLibrosDemo();
            }
        };

        const verFichaLibro = (libro) => {
            selectedLibro.value = libro;
            navegarA('detalle');
        };

        const solicitarPrestamo = async (libro) => {
            if (libro.stockDisponible > 0) {
                libro.stockDisponible--;
            }
            alert(`✓ ¡Préstamo Solicitado con Éxito!\n\nEl ejemplar "${libro.titulo}" ha sido reservado para ti por 24 horas. Acércate con tu DNI al mostrador de Atención al Usuario para el retiro físico.`);
            navegarA('prestamos');
        };

        const reservarLibro = (libro) => {
            alert(`📌 ¡Reserva Registrada en Cola!\n\nHas ingresado a la lista de espera para "${libro.titulo}".`);
        };

        const eliminarLibro = async (libro) => {
            if (!confirm(`¿Está seguro de que desea dar de baja y eliminar el libro "${libro.titulo}" del catálogo?`)) {
                return;
            }
            try {
                if (libro.idLibro) {
                    await axios.delete(`/api/libros/${libro.idLibro}`);
                }
            } catch (e) {}
            libros.value = libros.value.filter(l => (l.idLibro || l.isbn) !== (libro.idLibro || libro.isbn));
            alert(`✓ El libro "${libro.titulo}" ha sido dado de baja correctamente.`);
        };

        const guardarNuevoLibro = async (nuevo) => {
            const nuevoObj = {
                titulo: nuevo.titulo,
                isbn: nuevo.isbn,
                resumen: nuevo.resumen,
                editorial: nuevo.editorial,
                stockTotal: parseInt(nuevo.stockTotal) || 1,
                stockDisponible: parseInt(nuevo.stockTotal) || 1,
                anioPublicacion: 2024,
                autores: [{ nombres: nuevo.autores, apellidos: '' }],
                categorias: [{ nombre: nuevo.categoria }]
            };
            try {
                await axios.post('/api/libros', nuevoObj);
            } catch (e) {}
            libros.value.unshift(nuevoObj);
            alert(`✓ ¡Libro "${nuevo.titulo}" guardado e indexado con éxito!`);
        };

        function getLibrosDemo() {
            return [
                {
                    idLibro: 1,
                    titulo: 'Arquitectura Web con Spring Boot 3',
                    autores: [{ nombres: 'Martin', apellidos: 'Fowler' }, { nombres: 'Rod', apellidos: 'Johnson' }],
                    categorias: [{ nombre: 'Ingeniería de Software' }],
                    anioPublicacion: 2024,
                    isbn: '978-0134757599',
                    editorial: 'Addison-Wesley Professional',
                    stockTotal: 5,
                    stockDisponible: 3,
                    ubicacion: 'Estante A-12 (Pabellón de Ingeniería)',
                    resumen: 'Guía completa de diseño y construcción de arquitecturas backend robustas con Spring Boot 3.x, Spring Data JPA y PostgreSQL.',
                    coincidencia: '...implementación de API RESTful, Spring Data JPA y PostgreSQL...',
                    destacados: [
                        'Persistencia de datos y transacciones concurrentes con JPA y PostgreSQL.',
                        'Diseño de controladores RESTful limpios y consumo de servicios.',
                        'Arquitectura en capas, buenas prácticas de desarrollo y control de stock.'
                    ]
                },
                {
                    idLibro: 2,
                    titulo: 'Vue.js 3 y Diseño de Frontend Reactivo',
                    autores: [{ nombres: 'Evan', apellidos: 'You' }],
                    categorias: [{ nombre: 'Desarrollo Web' }],
                    anioPublicacion: 2023,
                    isbn: '978-1491950296',
                    editorial: "O'Reilly Media",
                    stockTotal: 3,
                    stockDisponible: 2,
                    ubicacion: 'Estante A-05',
                    resumen: 'Manual exhaustivo de desarrollo de aplicaciones SPA modernas utilizando Vue 3, Composition API y Bootstrap 5.',
                    coincidencia: '...comunicación fluida mediante SPA con backend REST...',
                    destacados: [
                        'Construcción de interfaces reactivas con Composition API y Vue 3.',
                        'Integración visual con componentes Bootstrap 5 y CSS moderno.',
                        'Consumo eficiente de servicios REST mediante clientes Axios.'
                    ]
                },
                {
                    idLibro: 3,
                    titulo: 'Bases de Datos PostgreSQL Avanzado',
                    autores: [{ nombres: 'C. J.', apellidos: 'Date' }],
                    categorias: [{ nombre: 'Bases de Datos' }],
                    anioPublicacion: 2022,
                    isbn: '978-0321197849',
                    editorial: 'Pearson Educación',
                    stockTotal: 4,
                    stockDisponible: 0,
                    ubicacion: 'Estante C-01',
                    resumen: 'Tratado sobre diseño relacional, normalización estricta en 3FN, transacciones ACID y consultas JPQL complejas.',
                    coincidencia: '...integridad referencial, claves foráneas y modelo 3FN...',
                    destacados: [
                        'Técnicas de normalización en Tercera Forma Normal (3FN).',
                        'Manejo de concurrencia y transacciones ACID en PostgreSQL.',
                        'Diseño de índices de alta velocidad y optimización de consultas.'
                    ]
                }
            ];
        }

        onMounted(() => {
            cargarLibros();
            selectedLibro.value = getLibrosDemo()[0];
        });

        return {
            currentView,
            activeComponent,
            currentUser,
            libros,
            selectedLibro,
            prestamosLector,
            devolucionesLector,
            prestamosAdmin,
            telemetria,
            navegarA,
            handleLogin,
            handleRegistro,
            cerrarSesion,
            verFichaLibro,
            solicitarPrestamo,
            reservarLibro,
            eliminarLibro,
            guardarNuevoLibro
        };
    }
});

app.mount('#app');

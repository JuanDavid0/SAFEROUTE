/**
 * Modal de Políticas de Privacidad y Términos y Condiciones
 * Conforme a la Ley 1581 de 2012 (Ley de Habeas Data - Colombia)
 */

'use client';

import { useState } from 'react';

interface PrivacyPolicyModalProps {
    isOpen: boolean;
    onClose: () => void;
    onAccept: (aceptaPoliticas: boolean, aceptaTerminos: boolean) => void;
}

export const PrivacyPolicyModal: React.FC<PrivacyPolicyModalProps> = ({
    isOpen,
    onClose,
    onAccept,
}) => {
    const [aceptaPoliticas, setAceptaPoliticas] = useState(false);
    const [aceptaTerminos, setAceptaTerminos] = useState(false);

    const handleAccept = () => {
        onAccept(aceptaPoliticas, aceptaTerminos);
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-container modal-privacidad">
                <div className="modal-header">
                    <h2 className="modal-titulo">🔒 Políticas de Privacidad y Términos</h2>
                    <button onClick={onClose} className="modal-close-btn">
                        ✕
                    </button>
                </div>

                <div className="modal-body modal-scrollable">
                    {/* Políticas de Privacidad */}
                    <section className="privacy-section">
                        <h3 className="privacy-title">📋 Políticas de Privacidad</h3>
                        
                        <div className="privacy-content">
                            <h4>1. Marco Legal</h4>
                            <p>
                                SafeRoute cumple con la <strong>Ley 1581 de 2012</strong> (Ley de Protección de Datos Personales - Habeas Data) 
                                y el <strong>Decreto 1377 de 2013</strong> de Colombia.
                            </p>

                            <h4>2. Datos Recopilados</h4>
                            <p>Recopilamos los siguientes datos personales:</p>
                            <ul>
                                <li><strong>Identificación:</strong> Nombre completo, cédula de ciudadanía, teléfono, dirección</li>
                                <li><strong>Transaccionales:</strong> Solicitudes de productos, pedidos, historial de transacciones</li>
                            </ul>

                            <h4>3. Finalidad del Tratamiento</h4>
                            <p>Sus datos serán utilizados para:</p>
                            <ul>
                                <li>Procesar solicitudes de productos y pedidos</li>
                                <li>Realizar entregas y confirmar recepción de productos</li>
                                <li>Comunicarnos con usted sobre el estado de sus pedidos</li>
                                <li>Generar reportes administrativos y contables</li>
                                <li>Cumplir con obligaciones legales y tributarias</li>
                            </ul>

                            <h4>4. Derechos del Titular</h4>
                            <p>Usted tiene derecho a:</p>
                            <ul>
                                <li><strong>Conocer, actualizar y rectificar</strong> sus datos personales</li>
                                <li><strong>Ser informado</strong> sobre el uso que se ha dado a sus datos</li>
                                <li><strong>Revocar la autorización</strong> y/o solicitar la supresión de datos</li>
                                <li><strong>Presentar quejas</strong> ante la Superintendencia de Industria y Comercio</li>
                            </ul>

                            <h4>5. Seguridad de los Datos</h4>
                            <p>
                                Implementamos medidas de seguridad técnicas y administrativas para proteger sus datos:
                            </p>
                            <ul>
                                <li>Encriptación de datos sensibles (contraseñas, tokens)</li>
                                <li>Autenticación mediante JWT y OTP</li>
                                <li>Control de acceso basado en roles (RBAC)</li>
                                <li>Auditoría y registro de operaciones críticas</li>
                            </ul>

                            <h4>6. Tiempo de Retención</h4>
                            <p>
                                Conservaremos sus datos personales por un periodo mínimo de <strong>5 años</strong>, 
                                conforme a las obligaciones tributarias y contables de Colombia. 
                                Posteriormente, serán eliminados de forma segura.
                            </p>

                            <h4>7. Transferencia de Datos</h4>
                            <p>
                                Sus datos pueden ser compartidos con terceros únicamente para:
                            </p>
                            <ul>
                                <li><strong>Twilio:</strong> Servicio de verificación OTP (SMS)</li>
                                <li><strong>Firebase:</strong> Notificaciones push y mensajería</li>
                            </ul>
                        </div>
                    </section>

                    {/* Términos y Condiciones */}
                    <section className="privacy-section">
                        <h3 className="privacy-title">📜 Términos y Condiciones</h3>
                        
                        <div className="privacy-content">
                            <h4>1. Uso del Servicio</h4>
                            <p>
                                Al utilizar SafeRoute, usted acepta realizar solicitudes de productos a través de nuestra plataforma. 
                                Toda solicitud está sujeta a disponibilidad y aprobación administrativa.
                            </p>

                            <h4>2. Responsabilidades del Usuario</h4>
                            <ul>
                                <li>Proporcionar información <strong>veraz y actualizada</strong></li>
                                <li>Mantener la confidencialidad de sus credenciales de acceso</li>
                                <li>Realizar pagos dentro de los plazos establecidos</li>
                                <li>Confirmar la recepción de productos entregados</li>
                            </ul>

                            <h4>3. Modificaciones de Solicitud</h4>
                            <p>
                                Las solicitudes pueden ser <strong>modificadas hasta 2 veces</strong> antes de la fecha límite de pago. 
                                Después de confirmar el pago, no se permiten modificaciones.
                            </p>

                            <h4>4. Cancelaciones y Devoluciones</h4>
                            <p>
                                Las solicitudes pueden ser canceladas antes de la confirmación de pago. 
                                Una vez pagadas, no se aceptan devoluciones salvo por defectos de producto.
                            </p>

                            <h4>5. Limitación de Responsabilidad</h4>
                            <p>
                                SafeRoute no se hace responsable por retrasos en entregas causados por fuerza mayor o 
                                información incorrecta proporcionada por el usuario.
                            </p>

                            <h4>6. Modificaciones a los Términos</h4>
                            <p>
                                Nos reservamos el derecho de modificar estos términos. Los cambios serán notificados 
                                a través de la plataforma.
                            </p>
                        </div>
                    </section>
                </div>

                {/* Checkboxes de Aceptación */}
                <div className="modal-footer privacy-footer">
                    <div className="privacy-checkboxes">
                        <label className="checkbox-label">
                            <input
                                type="checkbox"
                                checked={aceptaPoliticas}
                                onChange={(e) => setAceptaPoliticas(e.target.checked)}
                            />
                            <span>
                                He leído y acepto las <strong>Políticas de Privacidad</strong> conforme a la Ley 1581 de 2012
                            </span>
                        </label>

                        <label className="checkbox-label">
                            <input
                                type="checkbox"
                                checked={aceptaTerminos}
                                onChange={(e) => setAceptaTerminos(e.target.checked)}
                            />
                            <span>
                                He leído y acepto los <strong>Términos y Condiciones</strong> de uso
                            </span>
                        </label>
                    </div>

                    <div className="privacy-buttons">
                        <button
                            className="btn-cancelar"
                            onClick={onClose}
                        >
                            Cancelar
                        </button>
                        <button
                            className="btn-aceptar"
                            onClick={handleAccept}
                            disabled={!aceptaPoliticas || !aceptaTerminos}
                        >
                            Aceptar y Continuar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

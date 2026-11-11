import { Body, Container, Head, Heading, Html, Preview, Section, Text } from '@react-email/components';

type PasswordResetEmailProps = {
  code: string;
  minutes: string;
};

const GOLD = '#C1853C';

/**
 * Email con el código para restablecer la contraseña ("Olvidé mi contraseña").
 *
 * Igual que verification-code: `npm run export` genera el HTML y el texto plano con los
 * marcadores {{code}} y {{minutes}}, y Spring los sustituye al enviar.
 */
export default function PasswordResetEmail({ code, minutes }: PasswordResetEmailProps) {
  return (
    <Html lang="es">
      <Head />
      <Preview>{`Tu código para cambiar la contraseña de TOCO es ${code}`}</Preview>
      <Body style={{ backgroundColor: '#000000', fontFamily: 'Helvetica, Arial, sans-serif', margin: 0, padding: '32px 0' }}>
        <Container style={{ maxWidth: '480px', margin: '0 auto', padding: '32px 24px', border: `1px solid ${GOLD}`, borderRadius: '16px' }}>
          <Heading style={{ color: GOLD, fontSize: '28px', textAlign: 'center', margin: '0 0 24px' }}>TOCO</Heading>
          <Text style={{ color: '#ECECEC', fontSize: '16px', lineHeight: '24px', margin: '0 0 16px' }}>
            Usa este código para elegir una contraseña nueva:
          </Text>
          <Section style={{ backgroundColor: 'rgba(193, 133, 60, 0.15)', border: `1px solid ${GOLD}`, borderRadius: '12px', padding: '16px', margin: '0 0 16px' }}>
            <Text style={{ color: GOLD, fontSize: '36px', fontWeight: 'bold', letterSpacing: '10px', textAlign: 'center', margin: 0 }}>
              {code}
            </Text>
          </Section>
          <Text style={{ color: '#A3A3A3', fontSize: '14px', lineHeight: '20px', margin: 0 }}>
            {`El código caduca en ${minutes} minutos. Si no has pedido cambiar tu contraseña, ignora este email: tu contraseña actual sigue funcionando.`}
          </Text>
        </Container>
      </Body>
    </Html>
  );
}

// Valores de ejemplo para la vista previa de `npm run dev`
PasswordResetEmail.PreviewProps = { code: '730215', minutes: '10' } satisfies PasswordResetEmailProps;

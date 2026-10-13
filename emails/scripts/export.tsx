/**
 * Exporta las plantillas a src/main/resources/templates/email del backend, en HTML y texto plano,
 * dejando los marcadores {{...}} que Spring sustituye al enviar. Ejecutar tras cambiar una plantilla.
 */
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { render } from '@react-email/components';
import VerificationCodeEmail from '../emails/verification-code';

const OUT_DIR = join(dirname(fileURLToPath(import.meta.url)), '../../src/main/resources/templates/email');

const templates = {
  'verification-code': <VerificationCodeEmail code="{{code}}" minutes="{{minutes}}" />,
};

await mkdir(OUT_DIR, { recursive: true });
for (const [name, element] of Object.entries(templates)) {
  await writeFile(join(OUT_DIR, `${name}.html`), await render(element));
  await writeFile(join(OUT_DIR, `${name}.txt`), await render(element, { plainText: true }));
  console.log(`Exportada ${name} (.html y .txt)`);
}

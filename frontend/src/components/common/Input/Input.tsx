import React, { type InputHTMLAttributes, type TextareaHTMLAttributes } from 'react';
import styles from './Input.module.css';

interface BaseInputProps {
  label?: string;
  error?: string;
  required?: boolean;
}

export type InputProps = BaseInputProps &
  (
    | ({ multiline?: false } & InputHTMLAttributes<HTMLInputElement>)
    | ({ multiline: true } & TextareaHTMLAttributes<HTMLTextAreaElement>)
  );

export const Input: React.FC<InputProps> = ({
  label,
  error,
  required,
  multiline,
  className = '',
  ...props
}) => {
  const inputClassName = [
    styles.input,
    error && styles['input-error'],
    multiline && styles.textarea,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={styles['input-wrapper']}>
      {label && (
        <label className={`${styles['input-label']} ${required ? styles['input-label-required'] : ''}`}>
          {label}
        </label>
      )}
      {multiline ? (
        <textarea className={inputClassName} {...(props as TextareaHTMLAttributes<HTMLTextAreaElement>)} />
      ) : (
        <input className={inputClassName} {...(props as InputHTMLAttributes<HTMLInputElement>)} />
      )}
      {error && <div className={styles['input-error-message']}>{error}</div>}
    </div>
  );
};

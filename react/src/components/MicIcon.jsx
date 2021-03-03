import styled from "styled-components";
import mic from "../icons/mic.svg";
import simple from "../icons/simple_mic.svg";

const MicIcon = ({ type = "default" }) => {
  const src = type === "default" ? mic : simple;
  return <Icon src={src} />;
};

const Icon = styled.img``;

export default MicIcon;

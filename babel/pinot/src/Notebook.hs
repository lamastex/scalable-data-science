{-# LANGUAGE TemplateHaskell #-}
module Notebook where

import Data.Default (def)
import qualified Data.Default as D
import Data.Text (Text, unpack)
import qualified Data.ByteString.Lazy as B
import qualified Data.HashMap.Lazy as H
import Control.Lens hiding ((.:))
import qualified Text.Pandoc as P
import qualified Text.Pandoc.Builder as P

data Notebook = N { _nName     :: Text
                  , _nCommands :: [Command] }
  deriving (Eq, Show)

data Command = C { _cLanguage :: Text
                 , _cCommand  :: Text
                 , _cState    :: Maybe Result }
  deriving (Eq, Show)

data Result = RSuccess P.Blocks
            | RError Text
  deriving (Eq, Show)

makeLenses ''Notebook
makeLenses ''Command

success :: Command -> Maybe P.Blocks
success (C _ _ (Just (RSuccess p))) = Just p
success _ = Nothing

-- data Language = Java
--               | R
--               | Scala
--               | Python
--               | Haskell
--               | Other Text
--               deriving (Eq, Show)
